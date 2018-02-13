package onethreeseven.jclimod;


import onethreeseven.jclimod.command.ListCommands;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Tests the cli program and loading in commands and executing them.
 * @author Luke Bermingham
 */
public class CLIProgramTest {



    @Test
    public void doListCommands() {
        CLIProgram prog = new CLIProgram();
        List<Object> cliCommands = prog.jc.getCommands().get("listCommands").getObjects();
        for (Object cliCommand : cliCommands) {
            if(cliCommand instanceof ListCommands){
                System.out.println(cliCommand);
                Map<String, ArrayList<CLICommand>> mapOfCommands = ((ListCommands)cliCommand).getMapOfCommands();
                ArrayList<CLICommand> allCommands = new ArrayList<>();
                for (ArrayList<CLICommand> commands : mapOfCommands.values()) {
                    allCommands.addAll(commands);
                }
                Assert.assertTrue(allCommands.contains(cliCommand));
            }
        }
    }
}