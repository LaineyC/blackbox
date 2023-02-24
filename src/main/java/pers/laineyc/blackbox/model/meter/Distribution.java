package pers.laineyc.blackbox.model.meter;

import lombok.Data;
import java.util.concurrent.TimeUnit;

@Data
public class Distribution extends Meter{

    private Boolean percentileHistogram;

    private double[] percentiles;

    private Integer percentilePrecision;

    private double[] serviceLevelObjectives;

    private Double minimumExpectedValue;

    private Double maximumExpectedValue;

    private Double expiry;

    private Integer bufferLength;

    private TimeUnit timeUnit = TimeUnit.SECONDS;
}
