package io.github.splotycode.mosaik.valuetransformer.io;

import io.github.splotycode.mosaik.util.ValueTransformer;
import io.github.splotycode.mosaik.util.datafactory.DataFactory;
import io.github.splotycode.mosaik.valuetransformer.CommonData;
import io.github.splotycode.mosaik.valuetransformer.TransformException;

import java.io.File;

public class StringToFile extends ValueTransformer<String, File> {

    @Override
    public File transform(String input, DataFactory info) throws TransformException {
        File base = info.getDataDefault(CommonData.BASE_PATH, null);
        if (base == null) {
            return new File(input);
        }
        return new File(base, input);
    }

}
