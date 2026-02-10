package uk.gov.laa.gradle.springboot.starter.export

import java.util.regex.Pattern
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.yaml.snakeyaml.Yaml

class SpringBootStarterExportCodegenTasks {

  private static final String DEFAULT_PACKAGE = 'uk.gov.justice.laa.export.generated'
  private static final Pattern EXPORT_ALIAS_PATTERN =
      Pattern.compile(/(?i)\bas\s+"?([A-Za-z_][A-Za-z0-9_]*)"?\s*(,|\bfrom\b|$)/)

  static void registerSql(Project project) {
    if (project.tasks.findByName('generateExportSql') != null) {
      return
    }

    project.pluginManager.apply('java')

    def context = context(project)
    def generateExportSql = project.tasks.register('generateExportSql') {
      context.appConfigFiles.findAll { it.exists() }.each { inputs.file(it) }
      inputs.files(context.exportDefinitionFiles)
      outputs.dir(context.exportSqlGeneratedDir)
      doLast {
        def exportDir = context.exportSqlGeneratedDir.get().asFile
        exportDir.mkdirs()

        def yaml = new Yaml()
        def definitions = mergedDefinitions(yaml, context)
        if (definitions.isEmpty()) {
          project.logger.warn('No export definitions found; skipping SQL export code generation.')
          return
        }

        definitions.each { key, defn ->
          def sql = defn?.sql
          if (!sql) {
            return
          }
          if (!defn?.provider) {
            throw new GradleException("Export ${key} has sql but no provider")
          }
          def packageName = defn?.packageName ?: DEFAULT_PACKAGE
          def columns = defn?.columns ?: []
          def aliasKeys = extractAliases(sql)
          def columnKeys = columns.collect { it.key }
          if (aliasKeys.isEmpty() && columnKeys.isEmpty()) {
            throw new GradleException("Export ${key} could not determine columns from SQL")
          }

          def effectiveKeys = aliasKeys.isEmpty() ? columnKeys : aliasKeys
          if (!columnKeys.isEmpty() && !aliasKeys.isEmpty()) {
            def missing = columnKeys.findAll { !aliasKeys.contains(it) }
            effectiveKeys = effectiveKeys + missing
          }

          def outDir = new File(exportDir, packageName.replace('.', '/'))
          outDir.mkdirs()

          def providerClassName = key.split('[_-]').collect { it.capitalize() }.join('') + 'Provider'
          def source = renderProviderSource(providerClassName, packageName, key, defn.provider, sql, effectiveKeys)
          new File(outDir, "${providerClassName}.java").text = source
        }
      }
    }

    addGeneratedSourceDir(project, context.exportSqlGeneratedDir)
    project.tasks.named('compileJava') {
      dependsOn(generateExportSql)
    }
  }

  static void registerControllers(Project project) {
    if (project.tasks.findByName('generateExportControllers') != null) {
      return
    }

    project.pluginManager.apply('java')

    def context = context(project)
    def generateExportControllers = project.tasks.register('generateExportControllers') {
      context.appConfigFiles.findAll { it.exists() }.each { inputs.file(it) }
      inputs.files(context.exportDefinitionFiles)
      outputs.dir(context.exportControllerGeneratedDir)
      doLast {
        def exportDir = context.exportControllerGeneratedDir.get().asFile
        exportDir.mkdirs()

        def yaml = new Yaml()
        def definitions = mergedDefinitions(yaml, context)
        if (definitions.isEmpty()) {
          project.logger.warn('No export definitions found; skipping export controller generation.')
          return
        }

        definitions.each { key, defn ->
          def sql = defn?.sql
          if (!sql) {
            return
          }

          def packageName = defn?.packageName ?: DEFAULT_PACKAGE
          def className = key.split('[_-]').collect { it.capitalize() }.join('') + 'ExportController'
          def outDir = new File(exportDir, packageName.replace('.', '/'))
          outDir.mkdirs()

          def source = renderControllerSource(className, key, defn, sql)
          new File(outDir, "${className}.java").text = source
        }
      }
    }

    addGeneratedSourceDir(project, context.exportControllerGeneratedDir)
    project.tasks.named('compileJava') {
      dependsOn(generateExportControllers)
    }
  }

  static void registerAll(Project project) {
    registerSql(project)
    registerControllers(project)
  }

  private static Map context(Project project) {
    def exportDefinitionsDir = project.file('src/main/resources/export_definitions')
    def exportDefinitionFiles = project.fileTree(exportDefinitionsDir) {
      include '**/*.yml'
      include '**/*.yaml'
    }

    [
      exportDefinitionsDir: exportDefinitionsDir,
      exportDefinitionFiles: exportDefinitionFiles,
      appConfigFiles: [
        project.file('src/main/resources/application.yml'),
        project.file('src/main/resources/application.yaml')
      ],
      exportSqlGeneratedDir: project.layout.buildDirectory.dir('generated/export-sql'),
      exportControllerGeneratedDir: project.layout.buildDirectory.dir('generated/export-web')
    ]
  }

  private static void addGeneratedSourceDir(Project project, def dirProvider) {
    SourceSetContainer sourceSets = project.extensions.getByType(SourceSetContainer)
    sourceSets.named('main') {
      java.srcDir(dirProvider)
    }
  }

  private static Map mergedDefinitions(Yaml yaml, Map context) {
    def exportsConfig = loadExportsConfig(yaml, context.appConfigFiles)
    if (exportsConfig?.enabled != true) {
      return [:]
    }

    def definitions =
        loadDefinitionsFromFiles(yaml, context.exportDefinitionsDir, context.exportDefinitionFiles)
    def appDefinitions = exportsConfig?.definitions ?: [:]
    definitions.putAll(appDefinitions)
    return definitions
  }

  private static Map loadExportsConfig(Yaml yaml, List<File> appConfigFiles) {
    for (File appConfigFile : appConfigFiles) {
      if (!appConfigFile.exists()) {
        continue
      }
      def config = yaml.load(appConfigFile.text) ?: [:]
      def exportsConfig = config?.laa?.springboot?.starter?.exports
      if (!exportsConfig) {
        exportsConfig = config?.laa?.'springboot.starter'?.exports
      }
      if (exportsConfig) {
        return exportsConfig
      }
    }
    return [:]
  }

  private static Map loadDefinitionsFromFiles(
      Yaml yaml,
      File exportDefinitionsDir,
      def exportDefinitionFiles) {
    def defs = [:]
    if (!exportDefinitionsDir.exists() || exportDefinitionFiles.isEmpty()) {
      return defs
    }

    exportDefinitionFiles.each { file ->
      def data = yaml.load(file.text) ?: [:]
      def nestedDefs = data?.laa?.springboot?.starter?.exports?.definitions ?: [:]
      if (!nestedDefs) {
        nestedDefs = data?.laa?.'springboot.starter'?.exports?.definitions ?: [:]
      }
      if (nestedDefs) {
        defs.putAll(nestedDefs)
        return
      }
      if (data?.sql || data?.provider) {
        defs.put(filenameKey(file), data)
        return
      }
      if (data instanceof Map && !data.isEmpty()) {
        defs.putAll(data)
      }
    }

    defs
  }

  private static List<String> extractAliases(String sql) {
    def keys = []
    if (sql == null) {
      return keys
    }
    def matcher = EXPORT_ALIAS_PATTERN.matcher(sql)
    while (matcher.find()) {
      keys << matcher.group(1)
    }
    keys
  }

  private static String filenameKey(File file) {
    def name = file.name
    def dot = name.lastIndexOf('.')
    dot > 0 ? name.substring(0, dot) : name
  }

  private static String renderProviderSource(
      String providerClassName,
      String packageName,
      String key,
      String providerName,
      String sql,
      List<String> columnOrder) {
    def sb = new StringBuilder()
    sb << "package ${packageName};\n\n"
    sb << 'import jakarta.annotation.Generated;\n'
    sb << 'import java.io.OutputStream;\n'
    sb << 'import java.io.OutputStreamWriter;\n'
    sb << 'import java.io.Writer;\n'
    sb << 'import java.nio.charset.StandardCharsets;\n'
    sb << 'import java.util.HashMap;\n'
    sb << 'import java.util.List;\n'
    sb << 'import java.util.Map;\n'
    sb << 'import javax.sql.DataSource;\n'
    sb << 'import org.springframework.stereotype.Component;\n'
    sb << 'import uk.gov.justice.laa.export.ExportCsvProvider;\n'
    sb << 'import uk.gov.justice.laa.export.csv.CsvHeaderWriter;\n'
    sb << 'import uk.gov.justice.laa.export.datasource.postgres.PostgresCopyExporter;\n'
    sb << 'import uk.gov.justice.laa.export.model.ExportColumn;\n'
    sb << 'import uk.gov.justice.laa.export.model.ValidatedExportRequest;\n\n'

    sb << '/**\n'
    sb << " * Export provider for ${key}.\n"
    sb << ' */\n'
    sb << "@Component(\"${providerName}\")\n"
    sb << '@Generated("export-sql-codegen")\n'
    sb << "public class ${providerClassName} implements ExportCsvProvider {\n"

    def sqlLines = sql.readLines().collect { it.replace('\\\\', '\\\\\\\\').replace('"', '\\\\"') }
    sb << '  private static final String SQL =\n'
    sb << '      String.join("\\n",\n'
    sqlLines.eachWithIndex { line, idx ->
      def suffix = idx == sqlLines.size() - 1 ? '' : ','
      sb << "          \"${line}\"${suffix}\n"
    }
    sb << '      );\n'

    def values = columnOrder.collect { "\"${it}\"" }
    if (values.isEmpty()) {
      sb << '  private static final List<String> COLUMN_ORDER = List.of();\n'
    } else {
      sb << '  private static final List<String> COLUMN_ORDER =\n'
      sb << '      List.of(\n'
      values.eachWithIndex { value, idx ->
        def suffix = idx == values.size() - 1 ? '' : ','
        sb << "          ${value}${suffix}\n"
      }
      sb << '      );\n'
    }

    sb << '  private final PostgresCopyExporter copyExporter;\n\n'
    sb << "  public ${providerClassName}(DataSource dataSource) {\n"
    sb << '    this.copyExporter = new PostgresCopyExporter(dataSource);\n'
    sb << '  }\n\n'

    sb << '  @Override\n'
    sb << '  public long writeCsv(\n'
    sb << '      ValidatedExportRequest request,\n'
    sb << '      OutputStream out,\n'
    sb << '      List<ExportColumn> columns) {\n'
    sb << '    Map<String, Object> params = new HashMap<>();\n'
    sb << '    params.putAll(request.getParams());\n'
    sb << '    params.put("maxRows", request.getMaxRows());\n'
    sb << '    try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {\n'
    sb << '      boolean hasOverrides = columns != null && !columns.isEmpty();\n'
    sb << '      if (hasOverrides) {\n'
    sb << '        CsvHeaderWriter.writeHeader(writer, COLUMN_ORDER, columns);\n'
    sb << '      }\n'
    sb << '      boolean includeHeader = !hasOverrides;\n'
    sb << '      long rows = copyExporter.copyCsv(SQL, params, writer, includeHeader);\n'
    sb << '      writer.flush();\n'
    sb << '      return rows;\n'
    sb << '    } catch (Exception e) {\n'
    sb << '      throw new RuntimeException("CSV export failed", e);\n'
    sb << '    }\n'
    sb << '  }\n'
    sb << '}\n'

    sb.toString()
  }

  private static String renderControllerSource(
      String className,
      String key,
      Map defn,
      String sql) {
    def sb = new StringBuilder()
    def packageName = defn?.packageName ?: DEFAULT_PACKAGE

    sb << "package ${packageName};\n\n"
    sb << 'import io.swagger.v3.oas.annotations.Operation;\n'
    sb << 'import io.swagger.v3.oas.annotations.media.Content;\n'
    sb << 'import io.swagger.v3.oas.annotations.media.ExampleObject;\n'
    sb << 'import io.swagger.v3.oas.annotations.responses.ApiResponse;\n'
    sb << 'import java.time.LocalDate;\n'
    sb << 'import java.util.HashMap;\n'
    sb << 'import java.util.Map;\n'
    sb << 'import org.springframework.http.HttpHeaders;\n'
    sb << 'import org.springframework.http.ResponseEntity;\n'
    sb << 'import org.springframework.web.bind.annotation.GetMapping;\n'
    sb << 'import org.springframework.web.bind.annotation.RequestMapping;\n'
    sb << 'import org.springframework.web.bind.annotation.RequestParam;\n'
    sb << 'import org.springframework.web.bind.annotation.RestController;\n'
    sb << 'import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;\n'
    sb << 'import uk.gov.justice.laa.export.ExportService;\n\n'

    sb << '/**\n'
    sb << " * Generated export endpoint for ${key}.\n"
    sb << ' */\n'
    sb << '@RestController\n'
    sb << '@RequestMapping("${laa.springboot.starter.exports.web.base-path:/exports}")\n'
    sb << "public class ${className} {\n"
    sb << '  private final ExportService exportService;\n\n'
    sb << "  public ${className}(ExportService exportService) {\n"
    sb << '    this.exportService = exportService;\n'
    sb << '  }\n\n'

    def params = defn?.params ?: []
    def columns = defn?.columns ?: []
    def aliasKeys = extractAliases(sql)
    def headerKeys = aliasKeys
    if (!columns.isEmpty()) {
      def columnKeys = columns.collect { it.key }
      if (!aliasKeys.isEmpty()) {
        def missing = columnKeys.findAll { !aliasKeys.contains(it) }
        headerKeys = aliasKeys + missing
      } else {
        headerKeys = columnKeys
      }
    }

    def headerLine = ''
    if (!headerKeys.isEmpty()) {
      def overrides = [:]
      columns.each { c -> overrides[c.key] = c }
      def headers = []
      headerKeys.each { alias ->
        def override = overrides[alias]
        headers << (override?.header ?: alias)
      }
      headerLine = headers.join(',')
    }

    def methodName = 'export' + key.split(/[^A-Za-z0-9]+/).findAll { it }.collect { it.capitalize() }.join('')
    sb << "  @Operation(summary = \"Export ${key}\")\n"
    if (headerLine) {
      def escaped = headerLine.replace('"', '\\\\"')
      def chunks = escaped.collect { it }.collate(80).collect { it.join('') }
      sb << '  @ApiResponse(\n'
      sb << '      responseCode = "200",\n'
      sb << '      description = "CSV export",\n'
      sb << '      content = @Content(\n'
      sb << '          mediaType = "text/csv",\n'
      sb << '          examples = @ExampleObject(\n'
      sb << '              value =\n'
      chunks.eachWithIndex { chunk, idx ->
        def prefix = idx == 0 ? '                  ' : '                  + '
        sb << "${prefix}\"${chunk}\"\n"
      }
      sb << '          )\n'
      sb << '      )\n'
      sb << '  )\n'
    } else {
      sb << '  @ApiResponse(\n'
      sb << '      responseCode = "200",\n'
      sb << '      description = "CSV export",\n'
      sb << '      content = @Content(mediaType = "text/csv")\n'
      sb << '  )\n'
    }

    sb << "  @GetMapping(value = \"/${key}.csv\", produces = \"text/csv\")\n"
    sb << "  public ResponseEntity<StreamingResponseBody> ${methodName}(\n"

    def requestParams = []
    params.each { p ->
      def required = p.required == true
      def requiredAttr = required ? '' : ', required = false'
      requestParams << "      @RequestParam(name = \"${p.name}\"${requiredAttr}) String ${p.name}"
    }
    sb << requestParams.join(',\n')
    sb << '\n  ) {\n'
    sb << '    Map<String, String[]> rawParams = new HashMap<>();\n'
    params.each { p ->
      sb << "    if (${p.name} != null) {\n"
      sb << "      rawParams.put(\"${p.name}\", new String[] { ${p.name} });\n"
      sb << '    }\n'
    }
    sb << "    String filename = \"${key}-\" + LocalDate.now() + \".csv\";\n"
    sb << "    StreamingResponseBody body = out -> exportService.streamCsv(\"${key}\", rawParams, out);\n"
    sb << '    return ResponseEntity.ok()\n'
    sb << '        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\\\"" + filename + "\\\"")\n'
    sb << '        .header(HttpHeaders.CACHE_CONTROL, "no-store")\n'
    sb << '        .body(body);\n'
    sb << '  }\n'
    sb << '}\n'

    sb.toString()
  }
}
