package net.digitalid.database.property.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.generator.annotations.meta.Interceptor;
import net.digitalid.utility.generator.information.method.MethodInformation;
import net.digitalid.utility.generator.information.type.TypeInformation;
import net.digitalid.utility.generator.interceptor.MethodInterceptor;
import net.digitalid.utility.processor.generator.JavaFileGenerator;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.property.SubjectModule;
import net.digitalid.database.property.SubjectModuleBuilder;

/**
 * This method interceptor generates a subject module with the name of the surrounding class and its converter.
 * 
 * @see GeneratePersistentProperty
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(GenerateSubjectModule.Interceptor.class)
public @interface GenerateSubjectModule { // TODO: Move to the database interfaces so that the subject class can declare the method with this annotation?
    
    /**
     * This class generates the interceptor for the surrounding annotation.
     */
    @Stateless
    public static class Interceptor extends MethodInterceptor {
        
        @Pure
        @Override
        protected @Nonnull @NonEmpty String getPrefix() {
            return "implemented";
        }
        
        @Pure
        @Override
        public void generateFieldsRequiredByMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull TypeInformation typeInformation) {
            javaFileGenerator.addField("private static final @" + javaFileGenerator.importIfPossible(Nonnull.class) + " " + javaFileGenerator.importIfPossible(SubjectModule.class) + Brackets.inPointy(javaFileGenerator.importIfPossible(typeInformation.getType())) + " MODULE = " + javaFileGenerator.importIfPossible(SubjectModuleBuilder.class) + "." + Brackets.inPointy(javaFileGenerator.importIfPossible(typeInformation.getType())) + "withName" + Brackets.inRound(Quotes.inDouble(typeInformation.getName())) + ".withSubjectConverter" + Brackets.inRound(typeInformation.getSimpleNameOfGeneratedConverter() + ".INSTANCE") + ".build()");
        }
        
        @Pure
        @Override
        protected void implementInterceptorMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull String statement, @Nullable String resultVariable, @Nullable String defaultValue) {}
        
    }
    
}
