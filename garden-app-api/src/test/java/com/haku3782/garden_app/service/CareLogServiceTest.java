package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.CareLog;
import com.haku3782.garden_app.domain.Plant;
import com.haku3782.garden_app.domain.User;
import com.haku3782.garden_app.dto.CareLogRequest;
import com.haku3782.garden_app.dto.CareLogResponse;
import com.haku3782.garden_app.dto.PhotoGalleryItemResponse;
import com.haku3782.garden_app.repository.CareLogRepository;
import com.haku3782.garden_app.repository.PlantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareLogServiceTest {

    @Mock
    private CareLogRepository careLogRepository;

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @InjectMocks
    private CareLogService careLogService;

    @BeforeEach
    void setUpSecurityContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken("taro", null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Plant plantOwnedBy(String username, UUID plantId) {
        User owner = new User();
        owner.setUsername(username);
        Plant plant = new Plant();
        plant.setId(plantId);
        plant.setUser(owner);
        return plant;
    }

    @Test
    void getByPlantId_returnsLogsOrderedByCaredAtDesc() {
        UUID plantId = UUID.randomUUID();
        Plant plant = plantOwnedBy("taro", plantId);

        CareLog log = new CareLog();
        log.setId(UUID.randomUUID());
        log.setPlant(plant);
        log.setCareType("水やり");
        log.setCaredAt(LocalDateTime.of(2026, 6, 10, 8, 0));

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(careLogRepository.findByPlantIdOrderByCaredAtDesc(plantId)).thenReturn(List.of(log));

        List<CareLogResponse> result = careLogService.getByPlantId(plantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCareType()).isEqualTo("水やり");
        assertThat(result.get(0).getPlantId()).isEqualTo(plantId);
    }

    @Test
    void getByPlantId_throwsAccessDeniedWhenNotOwner() {
        UUID plantId = UUID.randomUUID();
        Plant ownedByOther = plantOwnedBy("hanako", plantId);

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(ownedByOther));

        assertThatThrownBy(() -> careLogService.getByPlantId(plantId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_throwsWhenPlantNotFound() {
        UUID missingPlantId = UUID.randomUUID();
        when(plantRepository.findById(missingPlantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careLogService.create(missingPlantId, new CareLogRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("見つかりません");
    }

    @Test
    void create_throwsAccessDeniedWhenNotOwner() {
        UUID plantId = UUID.randomUUID();
        Plant ownedByOther = plantOwnedBy("hanako", plantId);

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(ownedByOther));

        assertThatThrownBy(() -> careLogService.create(plantId, new CareLogRequest()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_savesCareLogLinkedToPlant() {
        UUID plantId = UUID.randomUUID();
        Plant plant = plantOwnedBy("taro", plantId);

        when(plantRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(careLogRepository.save(any(CareLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareLogRequest request = new CareLogRequest();
        request.setCareType("施肥");
        request.setCaredAt(LocalDateTime.of(2026, 6, 11, 9, 0));
        request.setMemo("液肥を追加");

        CareLogResponse response = careLogService.create(plantId, request);

        assertThat(response.getCareType()).isEqualTo("施肥");
        assertThat(response.getPlantId()).isEqualTo(plantId);
        assertThat(response.getMemo()).isEqualTo("液肥を追加");
    }

    @Test
    void update_throwsWhenCareLogNotFound() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(careLogRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careLogService.update(plantId, id, new CareLogRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("見つかりません");
    }

    @Test
    void update_throwsWhenPlantIdDoesNotMatch() {
        UUID plantId = UUID.randomUUID();
        UUID otherPlantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", otherPlantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.update(plantId, id, new CareLogRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("紐づくケアログ");
    }

    @Test
    void update_throwsAccessDeniedWhenNotOwner() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant ownedByOther = plantOwnedBy("hanako", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(ownedByOther);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.update(plantId, id, new CareLogRequest()))
                .isInstanceOf(AccessDeniedException.class);

        Mockito.verify(careLogRepository, Mockito.never()).save(any(CareLog.class));
    }

    @Test
    void update_appliesNewValuesWhenOwner() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);
        careLog.setCareType("水やり");
        careLog.setCaredAt(LocalDateTime.of(2026, 6, 10, 8, 0));

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));
        when(careLogRepository.save(any(CareLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareLogRequest request = new CareLogRequest();
        request.setCareType("施肥");
        request.setCaredAt(LocalDateTime.of(2026, 6, 11, 9, 30));
        request.setMemo("修正後のメモ");

        CareLogResponse response = careLogService.update(plantId, id, request);

        assertThat(response.getCareType()).isEqualTo("施肥");
        assertThat(response.getCaredAt()).isEqualTo(LocalDateTime.of(2026, 6, 11, 9, 30));
        assertThat(response.getMemo()).isEqualTo("修正後のメモ");
    }

    @Test
    void delete_throwsWhenCareLogNotFound() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(careLogRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careLogService.delete(plantId, id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("見つかりません");
    }

    @Test
    void delete_throwsWhenPlantIdDoesNotMatch() {
        UUID plantId = UUID.randomUUID();
        UUID otherPlantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", otherPlantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.delete(plantId, id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("紐づくケアログ");
    }

    @Test
    void delete_throwsAccessDeniedWhenNotOwner() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant ownedByOther = plantOwnedBy("hanako", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(ownedByOther);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.delete(plantId, id))
                .isInstanceOf(AccessDeniedException.class);

        Mockito.verify(careLogRepository, Mockito.never()).deleteById(id);
    }

    @Test
    void delete_delegatesToRepositoryWhenOwner() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        careLogService.delete(plantId, id);

        Mockito.verify(careLogRepository).deleteById(id);
    }

    @Test
    void uploadPhoto_savesPhotoUrlWhenValid() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);

        MockMultipartFile photo = new MockMultipartFile("photo", "flower.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));
        when(supabaseStorageService.upload(photo)).thenReturn("https://example.supabase.co/storage/v1/object/public/care-log-photos/abc.jpg");
        when(careLogRepository.save(any(CareLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareLogResponse response = careLogService.uploadPhoto(plantId, id, photo);

        assertThat(response.getPhotoUrl()).isEqualTo("https://example.supabase.co/storage/v1/object/public/care-log-photos/abc.jpg");
    }

    @Test
    void uploadPhoto_throwsWhenFileTooLarge() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);

        byte[] tooLarge = new byte[6 * 1024 * 1024];
        MockMultipartFile photo = new MockMultipartFile("photo", "big.jpg", "image/jpeg", tooLarge);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.uploadPhoto(plantId, id, photo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    void uploadPhoto_throwsWhenContentTypeNotAllowed() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);

        MockMultipartFile file = new MockMultipartFile("photo", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.uploadPhoto(plantId, id, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("対応していない");
    }

    @Test
    void uploadPhoto_throwsAccessDeniedWhenNotOwner() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant ownedByOther = plantOwnedBy("hanako", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(ownedByOther);

        MockMultipartFile photo = new MockMultipartFile("photo", "flower.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.uploadPhoto(plantId, id, photo))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deletePhoto_removesPhotoUrlAndCallsStorage() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);
        careLog.setPhotoUrl("https://example.supabase.co/storage/v1/object/public/care-log-photos/abc.jpg");

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));
        when(careLogRepository.save(any(CareLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareLogResponse response = careLogService.deletePhoto(plantId, id);

        assertThat(response.getPhotoUrl()).isNull();
        Mockito.verify(supabaseStorageService).delete("https://example.supabase.co/storage/v1/object/public/care-log-photos/abc.jpg");
    }

    @Test
    void deletePhoto_doesNothingWhenNoPhoto() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant plant = plantOwnedBy("taro", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(plant);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));
        when(careLogRepository.save(any(CareLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        careLogService.deletePhoto(plantId, id);

        Mockito.verify(supabaseStorageService, Mockito.never()).delete(any());
    }

    @Test
    void deletePhoto_throwsAccessDeniedWhenNotOwner() {
        UUID plantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Plant ownedByOther = plantOwnedBy("hanako", plantId);
        CareLog careLog = new CareLog();
        careLog.setId(id);
        careLog.setPlant(ownedByOther);

        when(careLogRepository.findById(id)).thenReturn(Optional.of(careLog));

        assertThatThrownBy(() -> careLogService.deletePhoto(plantId, id))
                .isInstanceOf(AccessDeniedException.class);

        Mockito.verify(supabaseStorageService, Mockito.never()).delete(any());
    }

    @Test
    void getPhotoGallery_returnsPhotosAcrossPlants() {
        UUID plantId = UUID.randomUUID();
        Plant plant = plantOwnedBy("taro", plantId);
        plant.setName("トマト");

        CareLog log = new CareLog();
        log.setId(UUID.randomUUID());
        log.setPlant(plant);
        log.setCareType("収穫");
        log.setCaredAt(LocalDateTime.of(2026, 6, 12, 10, 0));
        log.setPhotoUrl("https://example.supabase.co/storage/v1/object/public/care-log-photos/abc.jpg");

        when(careLogRepository.findByPlant_User_UsernameAndPhotoUrlIsNotNullOrderByCaredAtDesc("taro"))
                .thenReturn(List.of(log));

        List<PhotoGalleryItemResponse> result = careLogService.getPhotoGallery();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlantName()).isEqualTo("トマト");
        assertThat(result.get(0).getPhotoUrl()).isEqualTo("https://example.supabase.co/storage/v1/object/public/care-log-photos/abc.jpg");
    }
}
