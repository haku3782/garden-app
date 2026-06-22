package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.Plant;
import com.haku3782.garden_app.domain.PlantType;
import com.haku3782.garden_app.domain.User;
import com.haku3782.garden_app.dto.PlantRequest;
import com.haku3782.garden_app.dto.PlantResponse;
import com.haku3782.garden_app.repository.PlantRepository;
import com.haku3782.garden_app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class PlantServiceTest {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlantService plantService;

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

    @Test
    void getAll_returnsPlantsOfCurrentUser() {
        Plant plant = new Plant();
        plant.setId(UUID.randomUUID());
        plant.setName("トマト");
        plant.setType(PlantType.vegetable);

        when(plantRepository.findByUserUsername("taro")).thenReturn(List.of(plant));

        List<PlantResponse> result = plantService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("トマト");
    }

    @Test
    void create_savesPlantLinkedToCurrentUser() {
        User currentUser = new User();
        currentUser.setUsername("taro");
        when(userRepository.findByUsername("taro")).thenReturn(Optional.of(currentUser));

        PlantRequest request = new PlantRequest();
        request.setName("ナス");
        request.setType(PlantType.vegetable);
        request.setPlantedAt(LocalDateTime.of(2026, 6, 1, 9, 0));

        when(plantRepository.save(any(Plant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlantResponse response = plantService.create(request);

        assertThat(response.getName()).isEqualTo("ナス");
        assertThat(response.getType()).isEqualTo(PlantType.vegetable);
    }

    @Test
    void update_throwsWhenPlantNotFound() {
        UUID missingId = UUID.randomUUID();
        when(plantRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.update(missingId, new PlantRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("見つかりません");
    }

    @Test
    void update_appliesNewValuesToExistingPlant() {
        UUID id = UUID.randomUUID();
        Plant existing = new Plant();
        existing.setId(id);
        existing.setName("旧名前");
        existing.setType(PlantType.herb);

        when(plantRepository.findById(id)).thenReturn(Optional.of(existing));
        when(plantRepository.save(any(Plant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlantRequest request = new PlantRequest();
        request.setName("新名前");
        request.setType(PlantType.flower);

        PlantResponse response = plantService.update(id, request);

        assertThat(response.getName()).isEqualTo("新名前");
        assertThat(response.getType()).isEqualTo(PlantType.flower);
    }

    @Test
    void delete_delegatesToRepository() {
        UUID id = UUID.randomUUID();

        plantService.delete(id);

        org.mockito.Mockito.verify(plantRepository).deleteById(id);
    }
}
