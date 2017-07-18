package net.digitalid.database.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.testing.UtilityTest;
import net.digitalid.utility.validation.annotations.type.Immutable;

import org.junit.Test;

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateTableConverter
abstract class TestSubject extends RootClass implements Subject<Unit> {}

public class SubjectTest extends UtilityTest {
    
    @Test
    public void testGeneratedModule() {
        final @Nonnull TestSubject subject = TestSubjectBuilder.build();
        assertThat(subject.module().getName()).as("the name of the generated module").isEqualTo("TestSubject");
    }
    
}
