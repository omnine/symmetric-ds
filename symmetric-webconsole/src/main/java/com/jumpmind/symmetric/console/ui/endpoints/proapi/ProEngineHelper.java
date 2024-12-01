package com.jumpmind.symmetric.console.ui.endpoints.proapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jumpmind.symmetric.AbstractSymmetricEngine;
import org.jumpmind.symmetric.ISymmetricEngine;

public class ProEngineHelper {
    private String engineRegName;
    private ISymmetricEngine currentEngine;

    private Collection<ISymmetricEngine> getSymmetricEngines() {
        List<ISymmetricEngine> list = new ArrayList<>(AbstractSymmetricEngine.findEngines());
        list.sort((o1, o2) -> {
            if (o1.getNodeService().isRegistrationServer() && !o2.getNodeService().isRegistrationServer()) {
                return -1;
            } else {
                return !o1.getNodeService().isRegistrationServer() && o2.getNodeService().isRegistrationServer()
                        ? 1
                        : o1.getEngineName().compareTo(o2.getEngineName());
            }
        });
        return list;
    }


    public ISymmetricEngine getSymmetricEngine(String engineName) {
        for (ISymmetricEngine engine : this.getSymmetricEngines()) {
           if (engine.getEngineName().equals(engineName)) {
              return engine;
           }
        }
  
        return null;
     }    

    public ISymmetricEngine getSymmetricEngine() {
        if(currentEngine != null) {
            return currentEngine;
        }
        ISymmetricEngine engine = getSymmetricEngine(engineRegName);
        if (engine == null) {
            Collection<ISymmetricEngine> engines = getSymmetricEngines();
            if (engines != null && engines.size() > 0) {
                engine = engines.iterator().next();
                setSymmetricEngine(engine.getEngineName());
            }
        }

        currentEngine = engine;

        return engine;
    }

    public void setSymmetricEngine(String engineName) {
        this.engineRegName = engineName;
    }

}
