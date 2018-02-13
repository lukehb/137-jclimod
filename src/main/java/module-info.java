import onethreeseven.jclimod.AbstractCommandsListing;

/**
 * Java module for making a CLI program.
 */
module onethreeseven.jclimod {
    requires java.base;
    requires java.logging;
    requires jcommander;

    exports onethreeseven.jclimod;


    uses AbstractCommandsListing;

    provides onethreeseven.jclimod.AbstractCommandsListing with onethreeseven.jclimod.command.BasicCommandsListing;

    opens onethreeseven.jclimod to jcommander;
}