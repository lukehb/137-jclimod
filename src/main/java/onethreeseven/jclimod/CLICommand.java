package onethreeseven.jclimod;

import com.beust.jcommander.Parameter;
import java.lang.reflect.Field;


/**
 * The base class for all CLI commands.
 * @author Luke Bermingham
 */
public abstract class CLICommand {

    @Parameter(names = {"-h", "--help"}, help = true, description = "Append -h to any command to display usage tips.")
    private boolean runHelpCommand;

    protected abstract String getUsage();
    protected abstract boolean parametersValid();
    protected abstract boolean runImpl();

    protected void resetParametersAfterRun(Class clazz){
        for(Field field  : clazz.getDeclaredFields())
        {
            if (field.isAnnotationPresent(Parameter.class))
            {
                field.setAccessible(true);
                try {
                    if(!field.getType().isPrimitive()){
                        field.set(this, null);
                    }else{
                        if(field.getType().equals(boolean.class)){
                            field.setBoolean(this, false);
                        }
                        else{
                            field.set(this, 0);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if(clazz.getSuperclass() != null){
            resetParametersAfterRun(clazz.getSuperclass());
        }
    }

    public boolean run(){
        if(runHelpCommand){
            System.out.println("Example usage: " + getUsage());
            runHelpCommand = false;
            resetParametersAfterRun(this.getClass());
            return true;
        }
        else{
            if(parametersValid()){
                boolean success = runImpl();
                resetParametersAfterRun(this.getClass());
                return success;
            }else{
                System.err.println("Parameters invalid.");
                resetParametersAfterRun(this.getClass());
                return false;
            }
        }

    }

    public boolean askedForHelp() {
        return runHelpCommand;
    }

    public abstract String getCategory();

    public abstract String getCommandName();

    public abstract String[] getCommandNameAliases();

    public abstract String getDescription();

    @Override
    public String toString() {
        StringBuilder name = new StringBuilder(getCommandName());
        String[] secondaryCommandNames = getCommandNameAliases();
        if(secondaryCommandNames != null && secondaryCommandNames.length >= 1){
            for (String secondaryCommandName : secondaryCommandNames) {
                name.append("(").append(secondaryCommandName).append(")");
            }
        }
        return name.toString();
    }

}
