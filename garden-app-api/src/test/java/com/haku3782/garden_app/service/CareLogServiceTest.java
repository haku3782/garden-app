package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.CareLog;
import com.haku3782.garden_app.domain.Plant;
import com.haku3782.garden_app.dto.CareLogRequest;
import com.haku3782.garden_app.dto.CareLogResponse;
import com.haku3782.garden_app.repository.CareLogRepository;
import com.haku3782.garden_app.repository.PlantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private CareLogService careLogService;

    @Test
    void getByPlantId_returnsLogsOrderedByCaredAtDesc() {
        UUID plantId = UUID.randomUUID();
        Plant plant = new Plant();
        plant.setId(plantId);

        CareLog log = new CareLog();
        log.setId(UUID.randomUUID());
        log.setPlant(plant);
        log.setCareType("水やり");
        log.setCaredAt(LocalDateTime.of(2026, 6, 10, 8, 0));

        when(careLogRepository.findByPlantIdOrderByCaredAtDesc(plantId)).thenReturn(List.of(log));

        List<CareLogResponse> result = careLogService.getByPlantId(plantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCareType()).isEqualTo("水やり");
        assertThat(result.get(0).getPlantId()).isEqualTo(plantId);
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
    void create_savesCareLogLinkedToPlant() {
        UUID plantId = UUID.randomUUID();
        Plant plant = new Plant();
        plant.setId(plantId);

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
    void delete_delegatesToRepository() {
        UUID id = UUID.randomUUID();

        careLogService.delete(id);

        Mockito.verify(careLogRepository).deleteById(id);
    }
}
