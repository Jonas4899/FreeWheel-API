package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.VehicleDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VehicleService {
    VehicleDTO createVehicle(VehicleDTO vehicleDTO);
    VehicleDTO createVehicleWithDocuments(VehicleDTO vehicleDTO, MultipartFile licenciaTransito, MultipartFile soat, MultipartFile certificadoRevision, MultipartFile foto);

}
