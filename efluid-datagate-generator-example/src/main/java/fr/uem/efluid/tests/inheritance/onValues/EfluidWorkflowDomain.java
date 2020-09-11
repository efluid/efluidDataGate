package fr.uem.efluid.tests.inheritance.onValues;

import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.ParameterProject;
import fr.uem.efluid.ProjectColor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Based on efluid config
 */
@Documented
@Retention(CLASS)
@Target(TYPE)
@ParameterDomain(name = EfluidWorkflowDomain.NAME, project = @ParameterProject(name = EfluidWorkflowDomain.PROJECT, color = ProjectColor.BLUE))
public @interface EfluidWorkflowDomain {
    String NAME = "Workflow";
    String PROJECT = "Efluid Data";
}
