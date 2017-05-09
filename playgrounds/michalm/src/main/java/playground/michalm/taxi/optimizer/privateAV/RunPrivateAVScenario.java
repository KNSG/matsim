package playground.michalm.taxi.optimizer.privateAV;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.av.robotaxi.scoring.*;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.taxi.run.*;
import org.matsim.core.config.*;
import org.matsim.core.controler.*;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;


public class RunPrivateAVScenario {

  /**
   * This is the primary scenario runner it takes the following arguments:
   * @param args
   *   - configFile: The path to the scenario configuration file
   *   - runtaxi: if false, runs a standard scenario without any taxi infrastructure
   *   - privateav: if true, considers taxis as privately owned automated vehicles
   */
  public static void main(String[] args) {
    String configFile = args[0];
    boolean runTaxi = Boolean.parseBoolean(args[1]);
    boolean privateAV = Boolean.parseBoolean(args[2]);

    RunPrivateAVScenario.run(configFile, runTaxi, privateAV);
  }

  private static void run(String configFile, boolean runTaxi, boolean privateAV) {
    // TODO Auto-generated method stub
    Config config = ConfigUtils.loadConfig(configFile,
        new DvrpConfigGroup(),
        new TaxiConfigGroup(),
        new OTFVisConfigGroup(),
        new TaxiFareConfigGroup());

    /*
    config.network().setInputFile("network.xml.gz");
    config.facilities().setInputFile("facilities.xml.gz");
    config.plans().setInputFile("default_population.xml.gz");
    config.vehicles().setVehiclesFile("default_vehicles.xml.gz");

    config.controler().setRunId(runName);
    config.controler().setOutputDirectory("output/" + runName);
    */


    if(runTaxi){
      createTaxiControler(config, privateAV).run();
    } else {
      createDefaultControler(config).run();
    }

  }


  /**
   *
   * Create the scenario controler for an AV scenario
   *
   * @param config The scenario configuration
   * @param privateAV if true, then the taxi fleet is only available to
   *   the owners of the vehicles.
   * @return
   */
  public static Controler createTaxiControler(Config config, boolean privateAV){
    config.addConfigConsistencyChecker(new TaxiConfigConsistencyChecker());
    config.checkConsistency();

    Scenario scenario = ScenarioUtils.loadScenario(config);

    Controler controler = new Controler(scenario);
    controler.addOverridingModule(new AbstractModule() {
      @Override
      public void install() {
        addEventHandlerBinding().to(TaxiFareHandler.class).asEagerSingleton();
      }
    });
    controler.addOverridingModule(new TaxiOutputModule());

    if(privateAV){
      controler.addOverridingModule(new TaxiModule(PrivateATOptimizerProvider.class));
    } else {
      controler.addOverridingModule(new TaxiModule());
    }


    return controler;
  }

  /**
   * Create a basic controler without any taxi infrastructure
   *
   * @param config the scenario configuration
   * @return
   */
  public static Controler createDefaultControler(Config config){
    Scenario scenario = ScenarioUtils.loadScenario(config);
    Controler controler = new Controler(scenario);

    return controler;
  }


}
