package onethreeseven.jclimod;


import onethreeseven.jclimod.command.Exit;
import onethreeseven.jclimod.command.ListCommands;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests the cli program and loading in commands and executing them.
 * @author Luke Bermingham
 */
public class CLIProgramTest {



    @Test
    public void doListCommands() {
        CLIProgram prog = new CLIProgram();
        prog.addCommand(new ListCommands(prog.jc));
        prog.addCommand(new Exit());

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

    @Test
    public void testAliases(){

        final AtomicInteger nTimesCalled = new AtomicInteger();

        CLIProgram prog = new CLIProgram();
        Exit exitCommand = new Exit(){
            @Override
            protected boolean runImpl() {
                nTimesCalled.incrementAndGet();
                return true;
            }
        };
        prog.addCommand(exitCommand);

        String[] args = new String[]{"exit", "-t", "1"};
        prog.doCommand(args);

        //this should make an alias we can call
        String[] aliases = exitCommand.getRerunAliases();

        prog.doCommand(new String[]{"exit", "-rr", aliases[0]});

        Assert.assertTrue(nTimesCalled.get() == 2);


    }


}