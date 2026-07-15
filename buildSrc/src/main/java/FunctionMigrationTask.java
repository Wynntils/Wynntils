import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.fasterxml.jackson.core.PrettyPrinter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FunctionMigrationTask extends DefaultTask {

    @TaskAction
    public void run() {

        ParserConfiguration config = new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        StaticJavaParser.setConfiguration(config);

        File root = getProject().file("common/src/main/java");

        getLogger().lifecycle("Scanning: " + root.getAbsolutePath());

        if (!root.exists()) {
            getLogger().warn("Common source directory not found!");
            return;
        }

        getProject().fileTree(root).matching(pattern -> {
            pattern.include("**/*.java");
        }).forEach(file -> processFile(file));
    }

    private void processFile(File file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            LexicalPreservingPrinter.setup(cu);

            boolean changed = false;

            for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {

                boolean isFunction = clazz.getExtendedTypes().stream().anyMatch(type -> type.getNameAsString().equals("Function") || type.getNameAsString().equals("GenericFunction"));

                if (isFunction) {
                    ClassOrInterfaceDeclaration root = cu.getClassByName(file.getName().replace(".java", "")).get();

                    String name = clazz.getNameAsString().replace("Function", "");
                    name = name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();

                    String methodName = Character.toLowerCase(clazz.getNameAsString().charAt(0)) + clazz.getNameAsString().substring(1);

                    MethodDeclaration outputMethodDeclaration = root.addMethod(methodName);

                    NormalAnnotationExpr annotation = outputMethodDeclaration.addAndGetAnnotation("TemplateFunction").asNormalAnnotationExpr();

                    annotation.addPair("name", new StringLiteralExpr(name));

                    NodeList<Expression> aliasValues = new NodeList<>();

                    for (String alias : getAliases(clazz)) {
                        aliasValues.add(new StringLiteralExpr(alias));
                    }

                    if (aliasValues.size() > 0) annotation.addPair("aliases", new ArrayInitializerExpr(aliasValues));

                    MethodDeclaration getValueMethod = clazz.getMethodsByName("getValue").stream().findFirst().orElse(null);
                    if (getValueMethod == null) continue;
                    BlockStmt body = getValueMethod.getBody().orElseThrow();

                    HashMap<String, com.github.javaparser.ast.type.Type> arguments = getArguments(clazz);

                    replaceArgumentGets(body);
                    removeDuplicateParameterDeclarations(body, arguments);

                    for (Map.Entry<String, com.github.javaparser.ast.type.Type> entry : arguments.entrySet()) {
                        outputMethodDeclaration.addParameter(unboxType(entry.getValue()), entry.getKey());
                    }

                    outputMethodDeclaration.setBody(body);
                    outputMethodDeclaration.setModifier(Modifier.Keyword.PUBLIC, true);
                    outputMethodDeclaration.setType(unboxType(getValueMethod.getType()));

                    moveFieldsUp(clazz, root);

                    if (root.getAnnotationByName("SuppressWarnings").isEmpty()) {
                        addSuppressWarnings(root);
                    }

                    clazz.remove();
                    changed = true;
                }
            }

            if (changed) {
                cu.addImport("com.wynntils.templates.annotations.TemplateFunction");

                String result = StaticJavaParser.parse(new DefaultPrettyPrinter(new DefaultPrinterConfiguration()).print(cu)).toString();
                Files.writeString(Path.of("F:/output/" + file.getName()), result);
            }

        } catch (Exception e) {
            getLogger().error("Failed parsing: " + file.getPath(), e);
        }
    }

    private String[] getAliases(ClassOrInterfaceDeclaration clazz) {
        if (clazz.getMethodsByName("getAliases").stream().findFirst().isEmpty()) {
            return new String[0];
        }
        BlockStmt alliasesBody = clazz.getMethodsByName("getAliases").stream().findFirst().get().getBody().orElse(new BlockStmt());

        MethodCallExpr listOfCall = alliasesBody.getChildNodes().getFirst().stream().filter(MethodCallExpr.class::isInstance).map(MethodCallExpr.class::cast).findFirst().orElseThrow();

        return listOfCall.getArguments().stream().map(Expression::asStringLiteralExpr).map(StringLiteralExpr::getValue).toArray(String[]::new);
    }

    private void addSuppressWarnings(ClassOrInterfaceDeclaration clazz) {
        NormalAnnotationExpr annotation = clazz.addAndGetAnnotation("SuppressWarnings");
        annotation.addPair("value", new StringLiteralExpr("unused"));

        annotation.setLineComment("Functions are accessed via reflection");
    }

    private void replaceArgumentGets(BlockStmt body) {
        for (MethodCallExpr call : body.findAll(MethodCallExpr.class)) {
            if (!call.getNameAsString().endsWith("Value")) {
                continue;
            }

            Optional<Expression> scope = call.getScope();
            if (scope.isEmpty() || !scope.get().isMethodCallExpr()) {
                continue;
            }

            MethodCallExpr getArgument = scope.get().asMethodCallExpr();

            if (!getArgument.getNameAsString().equals("getArgument")) {
                continue;
            }

            if (getArgument.getScope().isEmpty() || !getArgument.getScope().get().isNameExpr() || !getArgument.getScope().get().asNameExpr().getNameAsString().equals("arguments")) {
                continue;
            }

            if (getArgument.getArguments().size() != 1 || !getArgument.getArgument(0).isStringLiteralExpr()) {
                continue;
            }

            String name = getArgument.getArgument(0).asStringLiteralExpr().getValue();

            call.replace(new NameExpr(name));
        }
    }

    private com.github.javaparser.ast.type.Type unboxType(com.github.javaparser.ast.type.Type t) {
        if (t.isClassOrInterfaceType()) {
            if (t.asClassOrInterfaceType().isBoxedType()) {
                return t.asClassOrInterfaceType().toUnboxedType();
            }
        }
        return t;
    }

    private void removeDuplicateParameterDeclarations(BlockStmt body, HashMap<String, com.github.javaparser.ast.type.Type> arguments) {
        for (VariableDeclarationExpr declaration : body.findAll(VariableDeclarationExpr.class)) {
            if (!(declaration.getParentNode().orElse(null) instanceof ExpressionStmt stmt)) {
                continue;
            }

            if (declaration.getVariables().size() != 1) {
                continue;
            }

            VariableDeclarator variable = declaration.getVariable(0);
            String name = variable.getNameAsString();

            if (!arguments.containsKey(name)) {
                continue;
            }

            if (variable.getInitializer().isEmpty()) {
                continue;
            }

            Expression initializer = variable.getInitializer().get();

            if (initializer.isNameExpr() && initializer.asNameExpr().getNameAsString().equals(name)) {
                stmt.remove();
                continue;
            }

            AssignExpr assignment = new AssignExpr(new NameExpr(name), initializer, AssignExpr.Operator.ASSIGN);

            stmt.replace(new ExpressionStmt(assignment));
        }
    }

    private void moveFieldsUp(ClassOrInterfaceDeclaration from, ClassOrInterfaceDeclaration to) {
        for (FieldDeclaration field : from.getFields()) {
            boolean exists = field.getVariables().stream().anyMatch(variable -> to.getFields().stream().anyMatch(existing -> existing.getVariable(0).getNameAsString().equals(variable.getNameAsString())));

            if (!exists) {
                to.getMembers().add(0, field.clone());
            }
        }
    }

    private HashMap<String, com.github.javaparser.ast.type.Type> getArguments(ClassOrInterfaceDeclaration clazz) {
        HashMap<String, com.github.javaparser.ast.type.Type> arguments = new HashMap<>();

        clazz.getMethodsByName("getArgumentsBuilder").stream().findFirst().ifPresent(method -> {
            BlockStmt body = method.getBody().orElseThrow();

            MethodCallExpr listOfCall = body.getChildNodes().getFirst().stream().filter(MethodCallExpr.class::isInstance).map(MethodCallExpr.class::cast).findFirst().orElseThrow();

            for (Expression expr : listOfCall.getArguments()) {
                if (expr.isObjectCreationExpr()) {
                    String name = expr.asObjectCreationExpr().getArgument(0).asStringLiteralExpr().getValue();

                    com.github.javaparser.ast.type.Type type = expr.asObjectCreationExpr().getArgument(1).asClassExpr().getType();
                    arguments.put(name, type);
                }
            }
        });

        return arguments;
    }
}