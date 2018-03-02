package onethreeseven.jclimod;

import com.beust.jcommander.Parameter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * The base class for all CLI commands.
 * @author Luke Bermingham
 */
public abstract class CLICommand {

    @Parameter(names = {"-rr", "--reRunCommand"}, description = "Runs the command using a previously used configuration that matches the passed in alias.")
    private String rerunCommandAlias;

    @Parameter(names = {"-la", "--listCommandAliases"}, description = "Lists all automatically generated aliases for running this command.")
    private boolean listCommandAliases;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Append -h to any command to display usage tips.")
    private boolean runHelpCommand;

    protected abstract String getUsage();
    protected abstract boolean parametersValid();
    protected abstract boolean runImpl();

    protected int getMaxStoredAliases(){
        return 5;
    }

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

    protected Preferences getAliasStore(){
        return Preferences.userNodeForPackage(this.getClass()).node(getCommandName());
    }

    private void storeCommandAlias(String[] args){
        //turn args into a single string
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg);
            sb.append(" ");
        }
        String alias = generateRerunAliasBasedOnParams();
        Preferences prefs = getAliasStore();




        try {

            //make sure we don't have more than max value keys
            String[] otherAliases = prefs.keys();
            if(otherAliases.length > getMaxStoredAliases()){
                List<String> keys = Arrays.asList(otherAliases);
                Collections.shuffle(keys);
                //+1 is for the new key we are about to add
                int nToRemove = (otherAliases.length - getMaxStoredAliases()) + 1 ;
                nToRemove = Math.min(otherAliases.length, nToRemove);
                for (int i = 0; i < nToRemove; i++) {
                    prefs.remove(keys.get(i));
                }
            }

            //actually store
            prefs.put(alias, sb.toString());
            //write
            prefs.flush();
            System.out.println("Tip: this command is now aliased, you can run it again by typing " + getCommandName() + " -rr " + alias);
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    private static final String commandNotAvailable = "none";

    protected boolean runPreviousCommandUsingAlias(String alias){
        resetParametersAfterRun(this.getClass());
        String prevCommandStr = getAliasStore().get(alias, commandNotAvailable);
        if(prevCommandStr.equals(commandNotAvailable)){
            System.err.println("There was no aliased command for " + getCommandName() + " called " + alias);
            return false;
        }
        if(prevCommandStr.contains("-rr") || prevCommandStr.contains("-reRunCommand")){
            System.err.println("Cannot run an aliased command that contains -rr or -reRunCommand, this will cause an infinite loop.");
            return false;
        }
        String[] commandArgs = prevCommandStr.split(" ");
        return new CLIProgram().addCommand(this).doCommand(commandArgs);
    }

    private void listAliases(){
        String[] aliases = getRerunAliases();
        if(aliases.length == 0){
            System.err.println("There were no aliases made yet for " + getCommandName());
        }
        else{
            System.out.println("Aliases for " + getCommandName());
            for (String childrenName : aliases) {
                System.out.println(childrenName + " (tip: type " + getCommandName() + " -rr " + childrenName + " to run this alias)");
            }
        }
    }

    public boolean run(String[] args){
        if(runHelpCommand){
            System.out.println("Example usage: " + getUsage());
            runHelpCommand = false;
            resetParametersAfterRun(this.getClass());
            return true;
        }
        else{
            if(listCommandAliases){
                listAliases();
                resetParametersAfterRun(this.getClass());
                return true;
            }
            else if(rerunCommandAlias != null){
                boolean success = runPreviousCommandUsingAlias(rerunCommandAlias);
                resetParametersAfterRun(this.getClass());
                return success;
            }
            else if(parametersValid()){
                boolean success = runImpl();
                if(success && shouldStoreRerunAlias()){
                    //store alias for the command so user can run it again quickly
                    storeCommandAlias(args);
                }
                resetParametersAfterRun(this.getClass());
                return success;
            }else{
                System.err.println("Parameters invalid.");
                resetParametersAfterRun(this.getClass());
                return false;
            }
        }

    }

    public void removeRerunAlias(String alias){
        getAliasStore().remove(alias);
    }

    public String[] getRerunAliases(){
        try {
            return getAliasStore().keys();
        } catch (BackingStoreException | IllegalStateException e) {
            e.printStackTrace();
            return new String[]{};
        }
    }

    public boolean askedForHelp() {
        return runHelpCommand;
    }

    /**
     * @return True if this command makes and stores automatic aliases when it is successfully executed.
     * This is an excellent time saver for users when commands have parameters. However, not all commands
     * or parameter combinations need aliases, so it is up to the inheritor of this abstract class to decide
     * whether the command should be aliased or not.
     */
    public abstract boolean shouldStoreRerunAlias();

    /**
     * This is a really useful method to implement because it will make an alias for any set of parameters
     * the user passes to your command. This will make running the same command again later much more
     * pleasant for the user because they just have to go yourCommand -rr someAlias
     * @return An automatic alias (name) based on the parameters that were passed in.
     */
    public abstract String generateRerunAliasBasedOnParams();

    public abstract String getCategory();

    public abstract String getCommandName();

    public abstract String[] getOtherCommandNames();

    public abstract String getDescription();

    @Override
    public String toString() {
        StringBuilder name = new StringBuilder(getCommandName());
        String[] secondaryCommandNames = getOtherCommandNames();
        if(secondaryCommandNames != null && secondaryCommandNames.length >= 1){
            for (String secondaryCommandName : secondaryCommandNames) {
                name.append("(").append(secondaryCommandName).append(")");
            }
        }
        return name.toString();
    }

}
