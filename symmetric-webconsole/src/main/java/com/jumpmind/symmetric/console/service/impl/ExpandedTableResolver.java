package com.jumpmind.symmetric.console.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.extension.IBuiltInExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.config.ITableResolver;
import org.jumpmind.symmetric.ext.ISymmetricEngineAware;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.service.INodeService;
import org.jumpmind.symmetric.service.impl.TriggerRouterContext;
import org.jumpmind.util.FormatUtils;

public class ExpandedTableResolver implements IBuiltInExtensionPoint, ITableResolver, ISymmetricEngineAware {
   public static final String TOKEN_TARGET_EXTERNAL_ID = "targetExternalId";
   private boolean cloneTableFromFirstNode;

   public void resolve(
      String catalog,
      String schema,
      Set<Table> tables,
      IDatabasePlatform platform,
      INodeService nodeService,
      Trigger trigger,
      boolean useTableCache,
      TriggerRouterContext triggerRouterContext
   ) {
      if (trigger.getSourceTableName().contains("targetExternalId")) {
         List<Node> nodes = new ArrayList<>();
         nodes.addAll(nodeService.findNodesWhoPullFromMe());
         nodes.addAll(nodeService.findNodesToPushTo());
         boolean isFirstNode = true;
         Table table = null;

         for (Node node : nodes) {
            String tableName = FormatUtils.replaceToken(trigger.getSourceTableName(), "targetExternalId", node.getExternalId(), true);
            if (isFirstNode || !this.cloneTableFromFirstNode) {
               table = platform.readTableFromDatabase(catalog, schema, tableName);
               isFirstNode = false;
               triggerRouterContext.incrementReadTableCount(trigger);
            } else if (table != null) {
               table = table.copy();
               table.setName(tableName);
               triggerRouterContext.incrementCopyTableCount(trigger);
            }

            if (table != null) {
               trigger.setSourceTableNameExpanded(true);
               tables.add(table);
            }
         }
      } else {
         Table table = platform.getTableFromCache(catalog, schema, trigger.getSourceTableNameUnescaped(), !useTableCache);
         if (table != null) {
            tables.add(table);
         }
      }
   }

   public void setSymmetricEngine(ISymmetricEngine engine) {
      this.cloneTableFromFirstNode = engine.getParameterService().is("sync.triggers.expand.table.clone", true);
   }
}
