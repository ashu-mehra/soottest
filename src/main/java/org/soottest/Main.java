package org.soottest;

import heros.InterproceduralCFG;
import picocli.CommandLine;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static soot.options.Options.output_format_jimple;

public class Main {
    static CmdlineOptions options = new CmdlineOptions();

    public static void main(String args[]) {
        new CommandLine(options).parseArgs(args);
        options.display();

        String jreDir = System.getProperty("java.home") + "/lib/jce.jar";
        String jceDir = System.getProperty("java.home") + "/lib/rt.jar";
        String path = jreDir + File.pathSeparator + jceDir + File.pathSeparator + options.classpath;
        System.out.println("soot classpath:" + path);
        Scene.v().setSootClassPath(path); //options.classpath);

        Options.v().set_output_format(output_format_jimple);
        // Enable whole-program mode
        Options.v().set_whole_program(true);
        Options.v().set_app(true);

        // Call-graph options
        Options.v().setPhaseOption("cg", "safe-newinstance:true");
        Options.v().setPhaseOption("cg.cha","enabled:true");

        // Enable SPARK call-graph construction
        Options.v().setPhaseOption("cg.spark","enabled:true");
        Options.v().setPhaseOption("cg.spark","verbose:true");
        Options.v().setPhaseOption("cg.spark","on-fly-cg:true");

        Options.v().set_allow_phantom_refs(true);
        List<String> excludePackagesList = Arrays.asList(new String[] {"java.*", "jdk.*", "jdk.internal.org.objectweb.asm.*"});
        Options.v().set_exclude(excludePackagesList);
        Options.v().set_no_bodies_for_excluded(true);

        // Set the main class of the application to be analysed
        Options.v().set_main_class(options.mainClass);

        // Load the main class
        SootClass c = Scene.v().loadClassAndSupport(options.mainClass);
        System.out.println(options.mainClass + " is phantom? " + c.isPhantom());
        c.setApplicationClass();

        Scene.v().loadNecessaryClasses();

        // Load the "main" method of the main class and set it as a Soot entry point
        List<SootMethod> entryPoints = options.entryPoints.stream().map(e -> c.getMethodByName(e)).collect(Collectors.toList());
        Scene.v().setEntryPoints(entryPoints);

        IFDSDataFlowTransformer uv = new IFDSDataFlowTransformer();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.herosifds", uv));

        PackManager.v().runPacks();

        showUninitializedVariables(uv, c.getMethodByName("<clinit>"));
        SootClass b = Scene.v().getSootClass("test1.B");
        showUninitializedVariables(uv, b.getMethodByName("<clinit>"));
        showUninitializedVariables(uv, b.getMethodByName("foo"));

        PackManager.v().writeOutput();
    }

    public static void showUninitializedVariables(IFDSDataFlowTransformer uv, SootMethod smethod) {
        System.out.println("Uninitialized variables for " + smethod);
        System.out.println(String.format("%-50s%s", "Statement", "Uninitialized variables"));
        for (Unit u : smethod.getActiveBody().getUnits()) {
            Set<SootField> variables = uv.getUninitializedVariablesAt(u);
            System.out.println(String.format("%-50s%s", u, variables));
        }
    }

    static class CmdlineOptions {
        @CommandLine.Option(names = "--classpath", description = "soot's classpath")
        String classpath;

        @CommandLine.Option(names = "--main-class", description = "application main class")
        String mainClass;

        @CommandLine.Option(names = "--entry-point", description = "entry points in main class")
        List<String> entryPoints;

        public void display() {
            System.out.println("Command line arguments:");
            System.out.println("\tclasspath: " + classpath);
            System.out.println("\tmain class: " + mainClass);
            System.out.println("\tentry points : " + entryPoints);
        }
    }
}
