package com.jumpmind.symmetric.console.service;

import com.jumpmind.symmetric.console.model.ConsoleEvent;
import java.util.List;
import org.jumpmind.extension.IExtensionPoint;
import org.jumpmind.symmetric.service.FilterCriterion;

public interface IConsoleEventService extends IExtensionPoint {
   void addEvent(ConsoleEvent var1);

   List<ConsoleEvent> getEvents(List<FilterCriterion> var1);
}
