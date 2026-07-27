package robertoCafagna.BE_capstone.initializers;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.services.BrandService;

@Component
@AllArgsConstructor
public class BrandInitializer implements CommandLineRunner {
    private final BrandService brandService;

    @Override
    public void run(String... args) throws Exception {

    }
}
