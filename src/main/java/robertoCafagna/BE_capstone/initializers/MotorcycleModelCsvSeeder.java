package robertoCafagna.BE_capstone.initializers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.entities.Brand;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;
import robertoCafagna.BE_capstone.repositories.GARAGE.MotorcycleModelRepository;
import robertoCafagna.BE_capstone.services.GARAGE.BrandService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
@Order(2)
@Slf4j
public class MotorcycleModelCsvSeeder implements CommandLineRunner {


    private static final Map<String, String> CSV_FILES = Map.ofEntries(
            Map.entry("Honda", "DATA/csv/ModelHonda.csv"),
            Map.entry("Yamaha", "DATA/csv/ModelYamaha.csv"),
            Map.entry("BMW Motorrad", "DATA/csv/ModelBMW.csv"),
            Map.entry("Piaggio", "DATA/csv/ModelPiaggio.csv"),
            Map.entry("Kawasaki", "DATA/csv/ModelKawasaki.csv"),
            Map.entry("Ducati", "DATA/csv/ModelDucati.csv"),
            Map.entry("KTM", "DATA/csv/ModelKTM.csv"),
            Map.entry("Suzuki", "DATA/csv/ModelSuzuki.csv"),
            Map.entry("Aprilia", "DATA/csv/ModelAprilia.csv"),
            Map.entry("Triumph", "DATA/csv/ModelTriumph.csv"),
            Map.entry("Moto Guzzi", "DATA/csv/ModelMotoGuzzi.csv"),
            Map.entry("Benelli", "DATA/csv/ModelBenelli.csv"),
            Map.entry("Royal Enfield", "DATA/csv/ModelRoyalEnfield.csv"),
            Map.entry("Harley-Davidson", "DATA/csv/ModelHarleyDavidson.csv"),
            Map.entry("Indian Motorcycle", "DATA/csv/ModelIndian.csv"),
            Map.entry("MV Agusta", "DATA/csv/ModelMVAgusta.csv"),
            Map.entry("Husqvarna", "DATA/csv/ModelHusqvarna.csv"),
            Map.entry("CFMOTO", "DATA/csv/ModelCFMOTO.csv"),
            Map.entry("Voge", "DATA/csv/ModelVoge.csv"),
            Map.entry("Zontes", "DATA/csv/ModelZontes.csv"),
            Map.entry("QJ Motor", "DATA/csv/ModelQJMotor.csv"),
            Map.entry("Fantic", "DATA/csv/ModelFantic.csv"),
            Map.entry("Beta", "DATA/csv/ModelBeta.csv"),
            Map.entry("GasGas", "DATA/csv/ModelGasGas.csv"),
            Map.entry("Derbi", "DATA/csv/ModelDerbi.csv"),
            Map.entry("Malaguti", "DATA/csv/ModelMalaguti.csv"),
            Map.entry("Kymco", "DATA/csv/ModelKymco.csv"),
            Map.entry("SYM", "DATA/csv/ModelSYM.csv"),
            Map.entry("Peugeot Motocycles", "DATA/csv/ModelPeugeotMotocycles.csv")

    );

    private final MotorcycleModelRepository motorcycleModelRepository;
    private final BrandService brandService;

    @Override
    public void run(String... args) throws Exception {
        for (Map.Entry<String, String> entry : CSV_FILES.entrySet()) {
            seedBrand(entry.getKey(), entry.getValue());
        }
    }

    private void seedBrand(String brandName, String csvPath) throws Exception {
        if (motorcycleModelRepository.existsByBrandName(brandName)) {
            log.info("Seeder modelli: {} già presente, salto", brandName);
            return;
        }

        Resource resource = new ClassPathResource(csvPath);
        Brand brand = brandService.findByName(brandName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            List<MotorcycleModel> models = reader.lines()
                    .skip(1)
                    .map(line -> parseLine(line, brand))
                    .toList();

            motorcycleModelRepository.saveAll(models);
            log.info("Seeder modelli: caricati {} modelli per {}", models.size(), brandName);
        }
    }

    private MotorcycleModel parseLine(String line, Brand brand) {
        String[] data = line.split(",", -1);
        return new MotorcycleModel(
                brand,
                data[1].trim(),
                parseInt(data[2]),
                MotorcycleCategory.valueOf(data[3].trim()),
                parseInt(data[4]),
                parseInteger(data[5]),
                parseInteger(data[6]),
                parseInteger(data[7]),
                data[8].isBlank() ? null : data[8].trim()
        );
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) return 0;
        return Integer.parseInt(value.trim());
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value.trim());
    }

}

