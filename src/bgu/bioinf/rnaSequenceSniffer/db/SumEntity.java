package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by matan on 07/04/15.
 */
@Entity
public class SumEntity extends Number {

    @Id
    @Column(name = "SUM")
    private Long sum;

    @Override
    public int intValue() {
        return sum.intValue();
    }

    @Override
    public long longValue() {
        return sum.longValue();
    }

    @Override
    public float floatValue() {
        return sum.floatValue();
    }

    @Override
    public double doubleValue() {
        return sum.doubleValue();
    }
}
