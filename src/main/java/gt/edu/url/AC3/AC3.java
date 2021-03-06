package gt.edu.url.AC3;

import gt.edu.url.util.FIFOQueue;

public class AC3 {
    public DomainRestore reduceDomains(CSPAC3 csp) {
        DomainRestore result = new DomainRestore();
        FIFOQueue<Variable> queue = new FIFOQueue<Variable>();
        for (Variable var : csp.getVariables())
            queue.add(var);
        reduceDomains(queue, csp, result);
        return result.compactify();
    }

    public DomainRestore reduceDomains(Variable var, Object value, CSPAC3 csp, Assignment assignment) {
        DomainRestore result = new DomainRestore();
        Domain domain = csp.getDomain(var);
        if (domain.contains(value)) {
            if (domain.size() > 1) {
                FIFOQueue<Variable> queue = new FIFOQueue<Variable>();
                queue.add(var);
                result.storeDomainFor(var, domain);
                csp.setDomain(var, new Domain(new Object[] { value }));
                reduceDomains(queue, csp, result, assignment);
            }
        } else {
            result.setEmptyDomainFound(true);
        }
        return result.compactify();
    }

    private void reduceDomains(FIFOQueue<Variable> queue, CSPAC3 csp,
                               DomainRestore info) {
        while (!queue.isEmpty()) {
            Variable var = queue.pop();
            for (ConstraintAC3 constraint : csp.getConstraints(var)) {
                if (constraint.getScope().size() == 2) {
                    Variable neighbor = csp.getNeighbor(var, constraint);
                    if (revise(neighbor, var, constraint, csp, info)) {
                        if (csp.getDomain(neighbor).isEmpty()) {
                            info.setEmptyDomainFound(true);
                            return;
                        }
                        queue.push(neighbor);
                    }
                }
            }
        }
    }

    /*
     * MAC Algorithm implementation
     */

    private void reduceDomains(FIFOQueue<Variable> queue, CSPAC3 csp,
                               DomainRestore info, Assignment assignment) {
        while (!queue.isEmpty()) {
            Variable var = queue.pop();
            for (ConstraintAC3 constraint : csp.getConstraints(var)) {
                if (constraint.getScope().size() == 2) {
                    Variable neighbor = csp.getNeighbor(var, constraint);
                    if (!assignment.hasAssignmentFor(neighbor)){
                        if (revise(neighbor, var, constraint, csp, info)) {
                            if (csp.getDomain(neighbor).isEmpty()) {
                                info.setEmptyDomainFound(true);
                                return;
                            }
                            queue.push(neighbor);
                        }
                    }
                }
            }
        }
    }

    private boolean revise(Variable xi, Variable xj, ConstraintAC3 constraint,
                           CSPAC3 csp, DomainRestore info) {
        boolean revised = false;
        Assignment assignment = new Assignment();
        for (Object iValue : csp.getDomain(xi)) {
            assignment.setAssignment(xi, iValue);
            boolean consistentExtensionFound = false;
            for (Object jValue : csp.getDomain(xj)) {
                assignment.setAssignment(xj, jValue);
                if (constraint.isSatisfiedWith(assignment)) {
                    consistentExtensionFound = true;
                    break;
                }
            }
            if (!consistentExtensionFound) {
                info.storeDomainFor(xi, csp.getDomain(xi));
                csp.removeValueFromDomain(xi, iValue);
                revised = true;
            }
        }
        return revised;
    }
}
