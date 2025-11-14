package com.clinica.clinica_coc.models;


public enum MotivoConsultaEnum {
    CARIES("Caries"),
    LIMPIEZA_DENTAL("Limpieza dental"),
    DOLOR_DENTAL("Dolor dental - de muela"),
    ORTODONCIA("Ortodoncia"),
    BLANQUEAMIENTO("Blanqueamiento de dientes"),
    GINGIVITIS("Gingivitis o Sangrado de encías"),
    FRACTURA_DENTAL("Fractura dental"),
    REVISION_PERIODICA("Revisión Periódica"),
    EXTRACCION_MUELAS_JUICIO("Extracción de muelas de juicio"),
    IMPLANTES("Caída de dientes e Implantes"),
    OTRO("Otro");

    private final String descripcion;

    MotivoConsultaEnum(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}