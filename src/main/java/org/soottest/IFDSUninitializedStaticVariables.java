package org.soottest;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2013 Eric Bodden and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.flowfunc.Kill;
import heros.flowfunc.KillAll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;

public class IFDSUninitializedStaticVariables
        extends DefaultJimpleIFDSTabulationProblem<SootField, InterproceduralCFG<Unit, SootMethod>> {

    public IFDSUninitializedStaticVariables(InterproceduralCFG<Unit, SootMethod> icfg) {
        super(icfg);
    }

    @Override
    public FlowFunctions<Unit, SootField, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Unit, SootField, SootMethod>() {

            @Override
            public FlowFunction<SootField> getNormalFlowFunction(Unit curr, Unit succ) {
                final SootMethod smethod = interproceduralCFG().getMethodOf(curr);

                if (Scene.v().getEntryPoints().contains(smethod) && interproceduralCFG().isStartPoint(curr)) {
                    return new FlowFunction<SootField>() {
                        @Override
                        public Set<SootField> computeTargets(SootField source) {
                            if (source == zeroValue()) {
                                Set<SootField> res = new LinkedHashSet<>(SootUtils.getStaticFields(smethod.getDeclaringClass()));
                                if (curr instanceof DefinitionStmt) {
                                    final Value leftOp = ((DefinitionStmt)curr).getLeftOp();
                                    if (leftOp instanceof StaticFieldRef) {
                                        final SootField defField = ((StaticFieldRef) leftOp).getField();
                                        res.remove(defField);
                                    }
                                }
                                return res;
                            }
                            return Collections.emptySet();
                        }
                    };
                }

                if (curr instanceof DefinitionStmt) {
                    final Value leftOp = ((DefinitionStmt)curr).getLeftOp();
                    if (leftOp instanceof StaticFieldRef) {
                        final SootField defField = ((StaticFieldRef)leftOp).getField();
                        return new Kill<>(defField);
                    }
                }

                return Identity.v();
            }

            @Override
            public FlowFunction<SootField> getCallFlowFunction(Unit callStmt, final SootMethod destinationMethod) {
                return Identity.v();
            }

            @Override
            public FlowFunction<SootField> getReturnFlowFunction(final Unit callSite, SootMethod calleeMethod, final Unit exitStmt,
                                                             Unit returnSite) {
                if (callSite instanceof DefinitionStmt) {
                    final Value leftOp = ((DefinitionStmt)callSite).getLeftOp();
                    if (leftOp instanceof StaticFieldRef) {
                        final SootField defField = ((StaticFieldRef)leftOp).getField();
                        return new Kill<>(defField);
                    }
                }

                return Identity.v();
            }

            @Override
            public FlowFunction<SootField> getCallToReturnFlowFunction(Unit callSite, Unit returnSite) {
                if (callSite instanceof DefinitionStmt) {
                    final Value leftOp = ((DefinitionStmt)callSite).getLeftOp();
                    if (leftOp instanceof StaticFieldRef) {
                        final SootField defField = ((StaticFieldRef)leftOp).getField();
                        return new Kill<>(defField);
                    }
                }
                return Identity.v();
            }
        };
    }

    public Map<Unit, Set<SootField>> initialSeeds() {
        return DefaultSeeds.make(Collections.singleton(Scene.v().getEntryPoints().get(0).getActiveBody().getUnits().getFirst()),
                zeroValue());
    }

    @Override
    public SootField createZeroValue() {
        return new SootField("@zero@", NullType.v());
    }
}


