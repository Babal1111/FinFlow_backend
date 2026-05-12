package com.finflow.application.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//        ModelMapper thoda "over-smart" banne ki koshish karta hai. Agar usse amount field mila,
//                aur DTO mein usse taxAmount mila, toh kabhi-kabhi woh guess kar leta hai ki
//        "Achha, dono mein 'amount' shabd hai, shayad yahi mapping hogi!"
//
//        Isse wrong data mapping ho sakti hai bina kisi error ke.'

        return modelMapper;
    }
}
