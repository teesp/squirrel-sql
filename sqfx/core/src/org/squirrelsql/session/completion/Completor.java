package org.squirrelsql.session.completion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.squirrelsql.session.ColumnInfo;
import org.squirrelsql.session.ProcedureInfo;
import org.squirrelsql.session.TableInfo;
import org.squirrelsql.session.completion.joingenerator.JoinGeneratorProvider;
import org.squirrelsql.session.parser.kernel.TableAliasInfo;
import org.squirrelsql.session.schemainfo.SchemaCacheProperty;
import org.squirrelsql.session.schemainfo.StructItemCatalog;
import org.squirrelsql.session.schemainfo.StructItemSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Completor
{
   private SchemaCacheProperty _schemaCacheValue;
   private TableAliasInfo[] _currentAliasInfos;
   private List<TableCompletionCandidate> _currentTableCandidatesNextToCursors = new ArrayList<>();

   public Completor(SchemaCacheProperty schemaCacheValue, List<TableInfo> currentTableInfosNextToCursor, TableAliasInfo[] currentAliasInfos)
   {
      _schemaCacheValue = schemaCacheValue;
      _currentAliasInfos = currentAliasInfos;

      for (TableInfo tableInfo : currentTableInfosNextToCursor)
      {
         _currentTableCandidatesNextToCursors.add(new TableCompletionCandidate(tableInfo, tableInfo.getStructItemSchema()));
      }
   }

   public ObservableList<CompletionCandidate> getCompletions(CaretVicinity caretVicinity)
   {
      List<CompletionCandidate> ret = new ArrayList<>();


      ret.addAll(JoinGeneratorProvider.getCandidates(caretVicinity));


      if(0 == caretVicinity.completedSplitsCount()) // everything
      {

         for (TableCompletionCandidate tableCompletionCandidate : _currentTableCandidatesNextToCursors)
         {
            for (ColumnInfo columnInfo : _schemaCacheValue.get().getColumns(tableCompletionCandidate.getTableInfo()))
            {
               if(caretVicinity.uncompletedSplitMatches(columnInfo.getColName()))
               {
                  ret.add(new ColumnCompletionCandidate(columnInfo, tableCompletionCandidate));
               }
            }
         }

         for (TableAliasInfo currentAliasInfo : _currentAliasInfos)
         {
            if(caretVicinity.uncompletedSplitMatches(currentAliasInfo.aliasName))
            {
               /////////////////////////////////////////////////////////////////////
               // For now we check duplicates for tables only.
               AliasCompletionCandidate aliasCompletionCandidate = new AliasCompletionCandidate(currentAliasInfo);
               //
               //////////////////////////////////////////////////////////////////////

               ret.add(aliasCompletionCandidate);
            }
         }


         for (String keyword : _schemaCacheValue.get().getDefaultKeywords())
         {
            if(caretVicinity.uncompletedSplitMatches(keyword))
            {
               ret.add(new KeywordCompletionCandidate(keyword));
            }
         }

         for (String keyword : _schemaCacheValue.get().getKeywords().getCellsAsString(0))
         {
            if(caretVicinity.uncompletedSplitMatches(keyword))
            {
               ret.add(new KeywordCompletionCandidate(keyword));
            }
         }

         List<StructItemCatalog> catalogs = _schemaCacheValue.get().getCatalogs();

         for (StructItemCatalog catalog : catalogs)
         {
            if(caretVicinity.uncompletedSplitMatches(catalog.getCatalog()))
            {
               ret.add(new CatalogCompletionCandidate(catalog));
            }
         }

         List<StructItemSchema> schemas = _schemaCacheValue.get().getSchemas();

         for (StructItemSchema schema : schemas)
         {
            if(caretVicinity.uncompletedSplitMatches(schema.getSchema()))
            {
               ret.add(new SchemaCompletionCandidate(schema));
            }
         }

         List<String> functions = _schemaCacheValue.get().getAllFunctions();

         for (String function : functions)
         {
            if(caretVicinity.uncompletedSplitMatches(function))
            {
               ret.add(new FunctionCompletionCandidate(function));
            }
         }

         // ???????
//         TableLoader<DataBaseType> types = _schemaCache.getTypes();
//
//         for (String function : functions)
//         {
//            if(tokenParser.uncompletedSplitMatches(function))
//            {
//               ret.add(new DataBaseTypeCompletionCandidate(function));
//            }
//         }

         fillTopLevelObjectsForSchemas(ret, caretVicinity, createFakeSchemaArrayForCatalog(null));

      }
      else if(1 == caretVicinity.completedSplitsCount()) // MyCatalog.xxx or MySchema.xxx or MyTable.xxx
      {
         StructItemCatalog catalog = _schemaCacheValue.get().getCatalogByName(caretVicinity.getCompletedSplitAt(0));

         if(null != catalog) // MyCatalog.xxx
         {
            fillTopLevelObjectsForSchemas(ret, caretVicinity, createFakeSchemaArrayForCatalog(catalog));
         }

         ///////////////////////////////////////////
         // MySchema.xxx
         List<StructItemSchema> schemas = _schemaCacheValue.get().getSchemasByName(caretVicinity.getCompletedSplitAt(0));

         fillTopLevelObjectsForSchemas(ret, caretVicinity, schemas);
         //
         ////////////////////////////////////////////

         ////////////////////////////////////////////////
         // MyTable.xxx
         fillColumnsForTable(ret, createFakeSchemaArrayForCatalog(null), caretVicinity.getCompletedSplitAt(0), caretVicinity);
         //
         //////////////////////////////////////////////////

         ////////////////////////////////////////////////
         // ALIAS.xxx
         for (TableAliasInfo currentAliasInfo : _currentAliasInfos)
         {
            if(caretVicinity.getCompletedSplitAt(0).equalsIgnoreCase(currentAliasInfo.aliasName))
            {
               List<TableInfo> tablesBySimpleName = _schemaCacheValue.get().getTablesBySimpleName(currentAliasInfo.tableName);
               for (TableInfo tableInfo : tablesBySimpleName)
               {
                  fillColumnsForTable(ret, Arrays.asList(tableInfo.getStructItemSchema()), tableInfo.getName(), caretVicinity);
               }
            }
         }
         //
         ///////////////////////////////////////////////////////


      }
      else if(2 == caretVicinity.completedSplitsCount()) // MyCatalog.MySchema,xxx or MyCatalog.MyTable.xxx or MySchema.MyTable.xxx
      {
         StructItemCatalog catalog = _schemaCacheValue.get().getCatalogByName(caretVicinity.getCompletedSplitAt(0));


         if (null != catalog) // MyCatalog.MySchema,xxx or MyCatalog.MyTable.xxx
         {
            List<StructItemSchema> schemas = _schemaCacheValue.get().getSchemaByNameAsArray(catalog.getCatalog(), caretVicinity.getCompletedSplitAt(1));

            fillTopLevelObjectsForSchemas(ret, caretVicinity, schemas);

            if(0 == schemas.size())
            {
               fillColumnsForTable(ret, createFakeSchemaArrayForCatalog(catalog), caretVicinity.getCompletedSplitAt(1), caretVicinity);
            }
         }
         else // MySchema.MyTable.xxx
         {
            List<StructItemSchema> schemas = _schemaCacheValue.get().getSchemasByName(caretVicinity.getCompletedSplitAt(0));
            fillColumnsForTable(ret, schemas, caretVicinity.getCompletedSplitAt(1), caretVicinity);
         }
      }
      else if(3 == caretVicinity.completedSplitsCount()) // MyCatalog.MySchema,MyTable.xxx
      {
         StructItemCatalog catalog = _schemaCacheValue.get().getCatalogByName(caretVicinity.getCompletedSplitAt(0));

         if(null != catalog)
         {
            List<StructItemSchema> schemas = _schemaCacheValue.get().getSchemaByNameAsArray(catalog.getCatalog(), caretVicinity.getCompletedSplitAt(1));

            fillColumnsForTable(ret, schemas, caretVicinity.getCompletedSplitAt(2), caretVicinity);
         }
      }

      return FXCollections.observableArrayList(ret);
   }

   private List<StructItemSchema> createFakeSchemaArrayForCatalog(StructItemCatalog catalog)
   {
      List<StructItemSchema> fakeSchemaArray = new ArrayList<>();

      StructItemSchema fakeSchema;

      if(null == catalog)
      {
         fakeSchema = new StructItemSchema(null, null);
      }
      else
      {
         fakeSchema = new StructItemSchema(null, catalog.getCatalog());
      }

      fakeSchemaArray.add(fakeSchema);
      return fakeSchemaArray;
   }

   private void fillColumnsForTable(List<CompletionCandidate> toFill, List<StructItemSchema> schemas, String tableName, CaretVicinity caretVicinity)
   {
      for (StructItemSchema schema : schemas)
      {
         List<TableInfo> tables;

         tables = _schemaCacheValue.get().getTablesByFullyQualifiedName(schema.getCatalog(), schema.getSchema(), tableName);

         fillMatchingCols(toFill, caretVicinity, tables, schema);

         if(tables.size() > 0)
         {
            return;
         }

         tables = _schemaCacheValue.get().getTablesBySchemaQualifiedName(schema.getSchema(), tableName);

         fillMatchingCols(toFill, caretVicinity, tables, schema);

         if(tables.size() > 0)
         {
            return;
         }

         tables = _schemaCacheValue.get().getTablesBySimpleName(tableName);

         fillMatchingCols(toFill, caretVicinity, tables, schema);
      }
   }

   private void fillMatchingCols(List<CompletionCandidate> ret, CaretVicinity caretVicinity, List<TableInfo> tables, StructItemSchema schema)
   {
      for (TableInfo table : tables)
      {

         for (ColumnInfo columnInfo : _schemaCacheValue.get().getColumns(table))
         {
            if(caretVicinity.uncompletedSplitMatches(columnInfo.getColName()))
            {
               ret.add(new ColumnCompletionCandidate(columnInfo, new TableCompletionCandidate(table, schema)));
            }
         }
      }
   }

   private void fillTopLevelObjectsForSchemas(List<CompletionCandidate> ret, CaretVicinity caretVicinity, List<StructItemSchema> schemas)
   {
      for (StructItemSchema schema : schemas)
      {
         List<TableInfo> tableInfos = _schemaCacheValue.get().getTableInfosMatching(schema.getCatalog(), schema.getSchema(), TableTypes.getTableAndView());

         DuplicateSimpleNamesCheck duplicateSimpleNamesCheck = new DuplicateSimpleNamesCheck();

         for (TableInfo tableInfo : tableInfos)
         {
            if(caretVicinity.uncompletedSplitMatches(tableInfo.getName()))
            {
               /////////////////////////////////////////////////////////////////////
               // For now we check duplicates for tables only.
               TableCompletionCandidate tableCompletionCandidate = new TableCompletionCandidate(tableInfo, schema);
               duplicateSimpleNamesCheck.check(tableCompletionCandidate);
               //
               //////////////////////////////////////////////////////////////////////

               ret.add(tableCompletionCandidate);
            }
         }

         List<ProcedureInfo> procedureInfos = _schemaCacheValue.get().getProcedureInfosMatching(schema.getCatalog(), schema.getSchema());

         for (ProcedureInfo procedureInfo : procedureInfos)
         {
            if(caretVicinity.uncompletedSplitMatches(procedureInfo.getName()))
            {
               ret.add(new ProcedureCompletionCandidate(procedureInfo, schema));
            }
         }

//         Looks bad for Postgres should be made configurable
//         ArrayList<UDTInfo> udtInfos = _schemaCache.getUDTInfosMatching(schema.getCatalog(), schema.getSchema());
//
//         for (UDTInfo udtInfo : udtInfos)
//         {
//            if(tokenParser.uncompletedSplitMatches(udtInfo.getName()))
//            {
//               ret.add(new UDTCompletionCandidate(udtInfo, schema));
//            }
//         }
      }
   }
}
