package com.wynntils.core.consumers.functions;

import com.wynntils.core.consumers.functions.expressions.ConstantExpression;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.expressions.FunctionExpression;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.functions.generic.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.List;

import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.reflect.Method;

public class TestTemplateCompiler {

    private MethodVisitor mv;
    private ClassWriter cw;
    private TraceClassVisitor tcv;

    @BeforeEach
    public void setup() {

        cw = new ClassWriter(
                ClassWriter.COMPUTE_FRAMES |
                        ClassWriter.COMPUTE_MAXS
        );


        tcv = new TraceClassVisitor(
                cw,
                new PrintWriter(System.out)
        );

        tcv.visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC,
                "CompiledTemplate",
                null,
                "java/lang/Object",
                null
        );

        mv = tcv.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "run",
                "()Ljava/lang/String;",
                null,
                null
        );

        mv.visitCode();
    }

    @Test
    public void cappedValue() throws Exception {
        String result = runFunction(
                new CappedFunctions.CappedFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(20)
                )
        );

        Assertions.assertEquals("4/20", result);
    }

    @Test
    public void cappedValueCurrent() throws Exception {
        String result = runFunction(
                new CappedFunctions.CurrentFunction(),
                List.of(
                        FunctionExpression.fromFunction(
                                new CappedFunctions.CappedFunction(),
                                List.of(
                                        ConstantExpression.fromObject(4),
                                        ConstantExpression.fromObject(20)
                                )
                        )
                )
        );

        Assertions.assertEquals("4", result);
    }

    @Test
    public void cappedValueMax() throws Exception {
        String result = runFunction(
                new CappedFunctions.CapFunction(),
                List.of(
                        FunctionExpression.fromFunction(
                                new CappedFunctions.CappedFunction(),
                                List.of(
                                        ConstantExpression.fromObject(4),
                                        ConstantExpression.fromObject(20)
                                )
                        )
                )
        );

        Assertions.assertEquals("20", result);
    }

    @Test
    public void cappedValueRemaining() throws Exception {
        String result = runFunction(
                new CappedFunctions.RemainingFunction(),
                List.of(
                        FunctionExpression.fromFunction(
                                new CappedFunctions.CappedFunction(),
                                List.of(
                                        ConstantExpression.fromObject(4),
                                        ConstantExpression.fromObject(20)
                                )
                        )
                )
        );

        Assertions.assertEquals("16", result);
    }

    @Test
    public void cappedValuePercentage() throws Exception {
        String result = runFunction(
                new CappedFunctions.PercentageFunction(),
                List.of(
                        FunctionExpression.fromFunction(
                                new CappedFunctions.CappedFunction(),
                                List.of(
                                        ConstantExpression.fromObject(4),
                                        ConstantExpression.fromObject(20)
                                )
                        )
                )
        );

        Assertions.assertEquals("20.0", result);
    }

    @Test
    public void cappedValueAtcap() throws Exception {
        String result = runFunction(
                new CappedFunctions.AtCapFunction(),
                List.of(
                        FunctionExpression.fromFunction(
                                new CappedFunctions.CappedFunction(),
                                List.of(
                                        ConstantExpression.fromObject(4),
                                        ConstantExpression.fromObject(20)
                                )
                        )
                )
        );

        Assertions.assertEquals("false", result);
    }

    @Test
    public void colorFromHex() throws Exception {
        String result = runFunction(
                new ColorFunctions.FromHexFunction(),
                List.of(
                        ConstantExpression.fromObject("FF00FF")
                )
        );

        Assertions.assertEquals("#ff00ffff", result);
    }

    @Test
    public void conditionalsIfTestPositive() throws Exception {

        String result = runFunction(
                new ConditionalFunctions.IfFunction(),
                List.of(
                        ConstantExpression.fromObject(true),
                        ConstantExpression.fromObject(1),
                        ConstantExpression.fromObject(2)
                )
        );

        Assertions.assertEquals("1", result);
    }

    @Test
    public void conditionalsIfTestDeep() throws Exception {

        runFunction(
                new ConditionalFunctions.IfFunction(),
                List.of(
                        FunctionExpression.fromFunction(new LogicFunctions.LessThanFunction(), List.of(
                                FunctionExpression.fromFunction(new MathFunctions.RandomFunction(), List.of(
                                        ConstantExpression.fromObject(0),
                                        ConstantExpression.fromObject(10)
                                )),
                                ConstantExpression.fromObject(5))),
                        FunctionExpression.fromFunction(new CappedFunctions.CappedFunction(), List.of(
                                ConstantExpression.fromObject(4),
                                ConstantExpression.fromObject(20)
                        )),
                        ConstantExpression.fromObject("Wassup")
                )
        );

        Assertions.assertTrue(true);
    }

    @Test
    public void conditionalsIfTestNegative() throws Exception {

        String result = runFunction(
                new ConditionalFunctions.IfFunction(),
                List.of(
                        ConstantExpression.fromObject(false),
                        ConstantExpression.fromObject(1),
                        ConstantExpression.fromObject(2)
                )
        );

        Assertions.assertEquals("2", result);
    }

    @Test
    public void conditionalsIfStringTestPositive() throws Exception {

        String result = runFunction(
                new ConditionalFunctions.IfStringFunction(),
                List.of(
                        ConstantExpression.fromObject(true),
                        ConstantExpression.fromObject("yes"),
                        ConstantExpression.fromObject("no")
                )
        );

        Assertions.assertEquals("yes", result);
    }

    @Test
    public void conditionalsIfStringTestNegative() throws Exception {

        String result = runFunction(
                new ConditionalFunctions.IfStringFunction(),
                List.of(
                        ConstantExpression.fromObject(false),
                        ConstantExpression.fromObject("yes"),
                        ConstantExpression.fromObject("no")
                )
        );

        Assertions.assertEquals("no", result);
    }

    @Test
    public void logicEqualsPositive() throws Exception {

        String result = runFunction(
                new LogicFunctions.EqualsFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(4.0)
                )
        );

        Assertions.assertEquals("true", result);
    }

    @Test
    public void logicEqualsNegative() throws Exception {

        String result = runFunction(
                new LogicFunctions.EqualsFunction(),
                List.of(
                        ConstantExpression.fromObject(0),
                        ConstantExpression.fromObject(4.0)
                )
        );

        Assertions.assertEquals("false", result);
    }


    @Test
    public void logicNotEquals() throws Exception {

        String result = runFunction(
                new LogicFunctions.NotEqualsFunction(),
                List.of(
                        ConstantExpression.fromObject(0),
                        ConstantExpression.fromObject(4.0)
                )
        );

        Assertions.assertEquals("true", result);
    }


    @Test
    public void logicNot() throws Exception {

        String result = runFunction(
                new LogicFunctions.NotFunction(),
                List.of(
                        ConstantExpression.fromObject(false)
                )
        );

        Assertions.assertEquals("true", result);
    }

    @Test
    public void logicAndPositive() throws Exception {

        String result = runFunction(
                new LogicFunctions.AndFunction(),
                List.of(
                        ConstantExpression.fromObject(true),
                        ConstantExpression.fromObject(true),
                        ConstantExpression.fromObject(true)
                )
        );

        Assertions.assertEquals("true", result);
    }

    @Test
    public void logicAndNegative() throws Exception {

        String result = runFunction(
                new LogicFunctions.AndFunction(),
                List.of(
                        ConstantExpression.fromObject(true),
                        ConstantExpression.fromObject(true),
                        ConstantExpression.fromObject(false)
                )
        );

        Assertions.assertEquals("false", result);
    }

    @Test
    public void logicLessThan() throws Exception {

        String result = runFunction(
                new LogicFunctions.LessThanFunction(),
                List.of(
                        ConstantExpression.fromObject(0),
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("true", result);
    }


    @Test
    public void logicLessThanOrEqual() throws Exception {

        String result = runFunction(
                new LogicFunctions.LessThanOrEqualsFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("true", result);
    }

    @Test
    public void logicGreaterThan() throws Exception {

        String result = runFunction(
                new LogicFunctions.GreaterThanFunction(),
                List.of(
                        ConstantExpression.fromObject(8),
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("true", result);
    }

    @Test
    public void logicGreaterThanOrEqual() throws Exception {

        String result = runFunction(
                new LogicFunctions.GreaterThanOrEqualsFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("true", result);
    }

    @Test
    public void mathAdd() throws Exception {

        String result = runFunction(
                new MathFunctions.AddFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("8.0", result);
    }

    @Test
    public void mathSub() throws Exception {

        String result = runFunction(
                new MathFunctions.SubtractFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("0.0", result);
    }

    @Test
    public void mathMul() throws Exception {

        String result = runFunction(
                new MathFunctions.MultiplyFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("16.0", result);
    }

    @Test
    public void mathDiv() throws Exception {

        String result = runFunction(
                new MathFunctions.DivideFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(2)
                )
        );

        Assertions.assertEquals("2.0", result);
    }

    @Test
    public void mathMod() throws Exception {

        String result = runFunction(
                new MathFunctions.ModuloFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(2)
                )
        );

        Assertions.assertEquals("0.0", result);
    }

    @Test
    public void mathPow() throws Exception {

        String result = runFunction(
                new MathFunctions.PowerFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(2)
                )
        );

        Assertions.assertEquals("16.0", result);
    }

    @Test
    public void mathSqrt() throws Exception {

        String result = runFunction(
                new MathFunctions.SquareRootFunction(),
                List.of(
                        ConstantExpression.fromObject(4)
                )
        );

        Assertions.assertEquals("2.0", result);
    }

    @Test
    public void mathMax() throws Exception {

        String result = runFunction(
                new MathFunctions.MaxFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(16),
                        ConstantExpression.fromObject(12)
                )
        );

        Assertions.assertEquals("16.0", result);
    }

    @Test
    public void mathMin() throws Exception {

        String result = runFunction(
                new MathFunctions.MinFunction(),
                List.of(
                        ConstantExpression.fromObject(4),
                        ConstantExpression.fromObject(16),
                        ConstantExpression.fromObject(12)
                )
        );

        Assertions.assertEquals("4.0", result);
    }

    @Test
    public void mathRound() throws Exception {

        String result = runFunction(
                new MathFunctions.RoundFunction(),
                List.of(
                        ConstantExpression.fromObject(4.23525),
                        ConstantExpression.fromObject(2)
                )
        );

        Assertions.assertEquals("4.24", result);
    }

    @Test
    public void mathInt() throws Exception {

        String result = runFunction(
                new MathFunctions.IntegerFunction(),
                List.of(
                        ConstantExpression.fromObject(4.23525)
                )
        );

        Assertions.assertEquals("4", result);
    }

    @Test
    public void mathLong() throws Exception {

        String result = runFunction(
                new MathFunctions.LongFunction(),
                List.of(
                        ConstantExpression.fromObject(4.23525)
                )
        );

        Assertions.assertEquals("4", result);
    }

    @Test
    public void mathRandom() throws Exception {

        String result = runFunction(
                new MathFunctions.RandomFunction(),
                List.of(
                        ConstantExpression.fromObject(0),
                        ConstantExpression.fromObject(8)
                )
        );

        double rs = Double.parseDouble(result);

        Assertions.assertTrue(rs >= 0 && rs < 8);
    }

    @Test
    public void stringString() throws Exception {

        String result = runFunction(
                new StringFunctions.StringFunction(),
                List.of(
                        ConstantExpression.fromObject(8)
                )
        );


        Assertions.assertEquals("8", result);
    }

    @Test
    public void stringContains() throws Exception {

        String result = runFunction(
                new StringFunctions.StringContainsFunction(),
                List.of(
                        ConstantExpression.fromObject("have happy day"),
                        ConstantExpression.fromObject("hap")

                )
        );


        Assertions.assertEquals("true", result);
    }


    @Test
    public void stringRegexMatch() throws Exception {

        String result = runFunction(
                new StringFunctions.RegexMatchFunction(),
                List.of(
                        ConstantExpression.fromObject("have happy day"),
                        ConstantExpression.fromObject(".*hap.*")

                )
        );


        Assertions.assertEquals("true", result);
    }

    @Test
    public void stringRegexReplace() throws Exception {

        String result = runFunction(
                new StringFunctions.RegexReplaceFunction(),
                List.of(
                        ConstantExpression.fromObject("have happy day"),
                        ConstantExpression.fromObject("a"),
                        ConstantExpression.fromObject("e")

                )
        );


        Assertions.assertEquals("heve heppy dey", result);
    }

    @Test
    public void styledTextStyledText() throws Exception {

        String result = runFunction(
                new StyledTextFunctions.StyledTextFunction(),
                List.of(
                        ConstantExpression.fromObject("HELLO")

                )
        );


        Assertions.assertEquals("StyledText{'HELLO'}", result);
    }

    @Test
    public void styledTextConcat() throws Exception {

        String result = runFunction(
                new StyledTextFunctions.ConcatStyledTextFunction(),
                List.of(
                        FunctionExpression.fromFunction(new StyledTextFunctions.StyledTextFunction(), List.of(ConstantExpression.fromObject("HELLO"))),
                        FunctionExpression.fromFunction(new StyledTextFunctions.StyledTextFunction(), List.of(ConstantExpression.fromObject(" WORLD")))

                )
        );


        Assertions.assertEquals("StyledText{'HELLO§{fr:minecraft:default} WORLD'}", result);
    }

    @Test
    public void styledTextWithColor() throws Exception {

        String result = runFunction(
                new StyledTextFunctions.WithColorFunction(),
                List.of(
                        FunctionExpression.fromFunction(new StyledTextFunctions.StyledTextFunction(), List.of(ConstantExpression.fromObject("HELLO"))),
                        FunctionExpression.fromFunction(new ColorFunctions.FromHexFunction(), List.of(ConstantExpression.fromObject("FF0000")))

                )
        );


        Assertions.assertEquals("StyledText{'§#ff0000ffHELLO'}", result);
    }

    private String runFunction(
            FunctionNode function,
            List<Expression> arguments
    ) throws Exception {

        Type resultType = function.emit(mv, arguments);

        TemplateCompiler.emitToString(resultType, mv);

        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        tcv.visitEnd();

        byte[] bytes = cw.toByteArray();

        CheckClassAdapter.verify(
                new ClassReader(bytes),
                false,
                new PrintWriter(System.out)
        );

        Class<?> clazz = new ClassLoader() {
            public Class<?> define(byte[] b) {
                return defineClass(null, b, 0, b.length);
            }
        }.define(bytes);


        Method method = clazz.getMethod("run");

        return (String) method.invoke(null);
    }
}