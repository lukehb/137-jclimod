package onethreeseven.jclimod;

import com.beust.jcommander.IStringConverter;
import java.io.File;

/**
 * Converts string input to file if you specify the use of this converter.
 * @author Luke Bermingham
 */
public class ParameterToFileConverter implements IStringConverter<File> {

    @Override
    public File convert(String value) {
        return new File(value);
    }

}
