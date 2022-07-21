package org.soottest;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;
import soot.Local;
import soot.SceneTransformer;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class IFDSDataFlowTransformer extends SceneTransformer {
    //private IFDSSolver<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver;
    private IFDSSolver<Unit, SootField, SootMethod, InterproceduralCFG<Unit, SootMethod>> solver;

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
        //icfg.setIncludePhantomCallees(true);
        //IFDSTabulationProblem<Unit, Local, SootMethod, InterproceduralCFG<Unit, SootMethod>> problem = new IFDSUninitializedVariables(icfg);
        IFDSTabulationProblem<Unit, SootField, SootMethod, InterproceduralCFG<Unit, SootMethod>> problem = new IFDSUninitializedStaticVariables(icfg);
        solver = new IFDSSolver<>(problem);

        System.out.println("Starting solver");
        solver.solve();
        System.out.println("Done");
    }

/*    public Set<Local> getUninitializedVariablesAt(Unit stmt) {
        Set<Local> result = solver.ifdsResultsAt(stmt);
        if (result != null) {
            return Collections.unmodifiableSet(solver.ifdsResultsAt(stmt));
        } else {
            return Collections.emptySet();
        }
    }*/

    public Set<SootField> getUninitializedVariablesAt(Unit stmt) {
        Set<SootField> result = solver.ifdsResultsAt(stmt);
        if (result != null) {
            return Collections.unmodifiableSet(solver.ifdsResultsAt(stmt));
        } else {
            return Collections.emptySet();
        }
    }
}
