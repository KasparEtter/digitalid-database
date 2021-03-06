/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.database.property.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.meta.Interceptor;
import net.digitalid.utility.generator.information.method.MethodInformation;
import net.digitalid.utility.generator.information.type.TypeInformation;
import net.digitalid.utility.generator.interceptor.MethodInterceptor;
import net.digitalid.utility.processing.logging.ProcessingLog;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.processing.utility.StaticProcessingEnvironment;
import net.digitalid.utility.processor.generator.JavaFileGenerator;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Provide;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.property.subject.Subject;

/**
 * This method interceptor generates a persistent property with the corresponding property table.
 * 
 * @see GenerateSubjectModule
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(GeneratePersistentProperty.Interceptor.class)
@TODO(task = "Implement the usage checks.", date = "2016-12-09", author = Author.KASPAR_ETTER)
public @interface GeneratePersistentProperty {
    
    /**
     * This class generates the interceptor for the surrounding annotation.
     */
    @Stateless
    public static class Interceptor extends MethodInterceptor {
        
        @Pure
        @Override
        protected @Nonnull String getPrefix() {
            return "implemented";
        }
        
        @Pure
        protected @Nonnull String getExternallyProvidedType(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull String simpleTypeName, @Nullable DeclaredType converter) {
            final @Nonnull String externallyProvidedType;
            if (simpleTypeName.endsWith("Role")) { // TODO: This hardcoding is only temporary, of course, and should be replaced as soon as the type information can be cached and retrieved.
                externallyProvidedType = javaFileGenerator.importIfPossible("net.digitalid.core.client.Client");
            } else if (simpleTypeName.equals("Context") || simpleTypeName.equals("Contact")) { // TODO: This hardcoding is only temporary, of course, and should be replaced as soon as the type information can be cached and retrieved.
                externallyProvidedType = javaFileGenerator.importIfPossible("net.digitalid.core.entity.NonHostEntity");
            } else if (simpleTypeName.equals("Student")) { // TODO: This hardcoding is only temporary, of course, and should be replaced as soon as the type information can be cached and retrieved.
                externallyProvidedType = javaFileGenerator.importIfPossible(Unit.class);
            } else if (converter != null) {
                externallyProvidedType = javaFileGenerator.importIfPossible(converter.getTypeArguments().get(1));
            } else {
                externallyProvidedType = "Void";
            }
            return externallyProvidedType;
        }
        
        @Pure
        private @Nullable DeclaredType getDeclaredConverterType(@Nonnull TypeMirror typeArgument) {
            final @Nonnull String valueConverterName = ProcessingUtility.getQualifiedName(typeArgument) + "Converter";
            final @Nullable TypeElement valueConverterElement = StaticProcessingEnvironment.getElementUtils().getTypeElement(valueConverterName);
            if (valueConverterElement == null) { ProcessingLog.warning("No type element was found for $, which might be because that type will only be generated in this round.", valueConverterElement); }
            final @Nullable DeclaredType valueConverterType = valueConverterElement == null ? null : ProcessingUtility.getSupertype((DeclaredType) valueConverterElement.asType(), Converter.class);
            return valueConverterType;
        }
        
        @Pure
        @Override
        @TODO(task = "Implement the value validation part as well!", date = "2016-12-09", author = Author.KASPAR_ETTER)
        public void generateFieldsRequiredByMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull TypeInformation typeInformation) {
//            final @Nonnull FreezableArrayList<@Nonnull Contract> contracts = FreezableArrayList.withNoElements();
//            
//            final @Nullable TypeMirror componentType = ProcessingUtility.getComponentType(method.getReturnType());
//            final @Nullable TypeElement typeElement = ProcessingUtility.getTypeElement(componentType);
//            final @Modifiable @Nonnull Map<@Nonnull AnnotationMirror, @Nonnull ValueAnnotationValidator> valueValidators = AnnotationHandlerUtility.getValueValidators(typeElement);
//            for (Map.@Nonnull Entry<@Nonnull AnnotationMirror, @Nonnull ValueAnnotationValidator> valueValidatorEntry : valueValidators.entrySet()) {
//                final @Nullable Contract contract = valueValidatorEntry.getValue().generateContract(typeElement, valueValidatorEntry.getKey(), javaFileGenerator);
//                contracts.add(contract);
//            }
//            
//            final @Nonnull String validationContent = contracts.map(contract -> javaFileGenerator.importIfPossible(Require.class) + ".that" + Brackets.inRound(contract.getCondition()) + ".orThrow" + Brackets.inRound(contract.getMessage() + ", " + contract.getArguments().join()) + ";").join("\n");
//            
//            javaFileGenerator.addField("private static final @" + javaFileGenerator.importIfPossible(Nonnull.class) + " " + javaFileGenerator.importIfPossible(FailableConsumer.class) + Brackets.inPointy(javaFileGenerator.importIfPossible(String.class) + ", " + javaFileGenerator.importIfPossible(PreconditionViolationException.class)) + method.getName().toUpperCase() + "_VALIDATOR = new " + javaFileGenerator.importIfPossible(FailableConsumer.class) + Brackets.inPointy(javaFileGenerator.importIfPossible(String.class) + ", " + javaFileGenerator.importIfPossible(PreconditionViolationException.class)) + "() {\n\n " +
//                    "@" + javaFileGenerator.importIfPossible(Impure.class) + "\n" +
//                    "@" + javaFileGenerator.importIfPossible(Override.class) + "\n" +
//                    "public void consume(@" + javaFileGenerator.importIfPossible(Captured.class) + javaFileGenerator.importIfPossible(String.class) + " password) throws " + javaFileGenerator.importIfPossible(PreconditionViolationException.class) + " {\n" + validationContent + "\n}" +
//            "}\n}");
            
            // TODO: Clean up the following mess!
            
            final @Nonnull String upperCasePropertyName = method.getName().toUpperCase();
            final @Nonnull String propertyPackage = ProcessingUtility.getQualifiedPackageName(((DeclaredType) method.getReturnType()).asElement());
            final @Nonnull String propertyType = "Persistent" + Strings.substringFromLast(ProcessingUtility.getSimpleName(method.getReturnType()), "Persistent");
            
            final @Nonnull List<@Nonnull ? extends TypeMirror> typeArguments = ((DeclaredType) method.getReturnType()).getTypeArguments();
            final @Nonnull String surroundingType = javaFileGenerator.importIfPossible(typeArguments.get(0)); // was before: typeInformation.getName();
            
            final @Nonnull String unitType = javaFileGenerator.importIfPossible(ProcessingUtility.getSupertype(typeInformation.getType(), Subject.class).getTypeArguments().get(0));
            
            final @Nonnull String withConverters;
            final @Nonnull String genericTypesTable;
            final @Nonnull String genericTypesProperty;
            final @Nonnull String genericTypesPropertyTable;
            if (propertyType.contains("Map")) {
                final @Nullable DeclaredType keyConverterType = getDeclaredConverterType(typeArguments.get(1));
                final @Nullable DeclaredType valueConverterType = getDeclaredConverterType(typeArguments.get(2));
                
                final @Nonnull String keyType = javaFileGenerator.importIfPossible(typeArguments.get(1));
                final @Nonnull String valueType = javaFileGenerator.importIfPossible(typeArguments.get(2));
                
                final @Nonnull String externallyProvidedTypeForKey = getExternallyProvidedType(javaFileGenerator, keyType, keyConverterType);
                final @Nonnull String externallyProvidedTypeForValue = getExternallyProvidedType(javaFileGenerator, valueType, valueConverterType);
                
                final @Nonnull String keyConverter = CustomType.importConverterType(typeArguments.get(1), FiniteIterable.of(), javaFileGenerator);
                final @Nonnull String valueConverter = CustomType.importConverterType(typeArguments.get(2), FiniteIterable.of(), javaFileGenerator);
                
                withConverters = ".withKeyConverter" + Brackets.inRound(keyConverter) + ".withValueConverter" + Brackets.inRound(valueConverter);
                genericTypesTable = Brackets.inPointy(unitType + ", " + surroundingType + ", " + keyType + ", " + valueType + ", " + externallyProvidedTypeForKey + ", " + externallyProvidedTypeForValue);
                
                genericTypesProperty = Brackets.inPointy(surroundingType + ", " + keyType + ", " + valueType);
                genericTypesPropertyTable = Brackets.inPointy(unitType + ", " + surroundingType + ", " + keyType + ", " + valueType);
            } else {
                final @Nullable DeclaredType valueConverterType = getDeclaredConverterType(typeArguments.get(1));
                
                final @Nonnull String valueType = javaFileGenerator.importIfPossible(typeArguments.get(1));
                
                final @Nonnull String externallyProvidedType = getExternallyProvidedType(javaFileGenerator, valueType, valueConverterType);
                
                final @Nonnull String valueConverter = CustomType.importConverterType(typeArguments.get(1), FiniteIterable.of(), javaFileGenerator);
                
                withConverters = ".withValueConverter" + Brackets.inRound(valueConverter);
                genericTypesTable = Brackets.inPointy(unitType + ", " + surroundingType + ", " + valueType + ", " + externallyProvidedType);
                genericTypesProperty = Brackets.inPointy(surroundingType + ", " + valueType);
                genericTypesPropertyTable = Brackets.inPointy(unitType + ", " + surroundingType + ", " + valueType);
            }
            
            final @Nonnull String defaultValue = ".withDefaultValue" + Brackets.inRound(method.hasAnnotation(Default.class) ? method.getAnnotation(Default.class).value() : "null");
            final @Nonnull String providedObject = method.hasAnnotation(Provide.class) ? ".withProvidedObjectExtractor" + Brackets.inRound(method.getAnnotation(Provide.class).value()) : "";
            
            final @Nonnull StringBuilder tableField = new StringBuilder("private static final @");
            tableField.append(javaFileGenerator.importIfPossible(Nonnull.class));
            tableField.append(" ");
            tableField.append(javaFileGenerator.importIfPossible(propertyPackage + "." + propertyType.replace("Simple", "") + "Table"));
            tableField.append(genericTypesTable);
            tableField.append(" ");
            tableField.append(upperCasePropertyName);
            tableField.append("_TABLE = ");
            tableField.append(javaFileGenerator.importIfPossible(propertyPackage + "." + propertyType.replace("Simple", "") + "TableBuilder"));
            tableField.append(".");
            tableField.append(genericTypesTable);
            tableField.append("withName(");
            tableField.append(Quotes.inDouble(method.getName()));
            tableField.append(").withParentModule(MODULE)");
            tableField.append(withConverters);
            tableField.append(propertyType.contains("Value") ? defaultValue : "");
            tableField.append(providedObject);
            tableField.append(".build()");
            javaFileGenerator.addField(tableField.toString());
            
            
            final @Nonnull StringBuilder propertyField = new StringBuilder("private final @");
            propertyField.append(javaFileGenerator.importIfPossible(Nonnull.class));
            propertyField.append(" ");
            propertyField.append(javaFileGenerator.importIfPossible(propertyPackage + ".Writable" + propertyType));
            propertyField.append(genericTypesProperty);
            propertyField.append(" ");
            propertyField.append(method.getName());
            propertyField.append(" = ");
            propertyField.append(javaFileGenerator.importIfPossible(propertyPackage + ".Writable" + propertyType + "ImplementationBuilder"));
            propertyField.append(".");
            propertyField.append(genericTypesPropertyTable);
            propertyField.append("withSubject(this).withTable(");
            propertyField.append(upperCasePropertyName);
            propertyField.append("_TABLE).build()");
            javaFileGenerator.addField(propertyField.toString());
        }
        
        @Pure
        @Override
        protected void implementInterceptorMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull String statement, @Nullable String resultVariable, @Nullable String defaultValue) {
            javaFileGenerator.addStatement("return " + method.getName());
        }
        
    }
    
}
