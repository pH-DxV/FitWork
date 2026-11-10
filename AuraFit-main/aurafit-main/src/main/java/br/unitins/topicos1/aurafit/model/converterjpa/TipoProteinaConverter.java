package br.unitins.topicos1.aurafit.model.converterjpa;

import br.unitins.topicos1.aurafit.model.TipoProteina;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoProteinaConverter implements AttributeConverter<TipoProteina, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoProteina tipo) {
        return tipo == null ? null : tipo.getId();
    }

    @Override
    public TipoProteina convertToEntityAttribute(Long id) {
        return TipoProteina.valueOf(id);
    }
}
