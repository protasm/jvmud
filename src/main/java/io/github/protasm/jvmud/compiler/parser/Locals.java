package io.github.protasm.jvmud.compiler.parser;

import java.util.ListIterator;
import java.util.Stack;

import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;

public class Locals {
    private final Stack<ASTLocal> locals;
    private final Stack<Integer> scopeIds;
    private int workingScopeDepth;
    private int nextScopeId;

    public Locals() {
        locals = new Stack<>();
        scopeIds = new Stack<>();
        scopeIds.push(0);
        workingScopeDepth = 0;
        nextScopeId = 1;
    }

    public Stack<ASTLocal> locals() {
        return locals;
    }

    public ASTLocal get(String name) {
        ListIterator<ASTLocal> localsItr = locals.listIterator(locals.size());

        while (localsItr.hasPrevious()) {
            ASTLocal local = localsItr.previous();

            if (local.symbol().name().equals(name))
                return local;
        }

        return null;
    }

    public void add(ASTLocal local) {
        locals.push(local);

        local.setScopeDepth(workingScopeDepth);
        local.setScopeId(scopeIds.peek());
    }

    /** Opens a parser local scope with a stable identity distinct from sibling scopes. */
    public void beginScope() {
        workingScopeDepth += 1;
        scopeIds.push(nextScopeId++);
    }

    /** Closes the current parser local scope and forgets locals declared inside it. */
    public void endScope() {
        workingScopeDepth -= 1;
        scopeIds.pop();

        // pop all locals belonging to the expiring scope
        while (!(locals.isEmpty()) && (locals().peek().scopeDepth() > workingScopeDepth))
            locals.pop();
    }
}
