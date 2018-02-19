import onethreeseven.jclimod.AbstractCommandsListing;

/**
 * Java module for making a CLI program.
 */
module onethreeseven.jclimod {
    requires java.base;
    requires java.logging;
    requires jcommander;
    requires java.prefs;

    exports onethreeseven.jclimod;
    exports onethreeseven.jclimod.command;


    uses AbstractCommandsListing;

    provides onethreeseven.jclimod.AbstractCommandsListing with onethreeseven.jclimod.command.BasicCommandsListing;

    opens onethreeseven.jclimod to jcommander;
    opens onethreeseven.jclimod.command to jcommander;
}