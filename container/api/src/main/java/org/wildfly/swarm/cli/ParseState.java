package org.wildfly.swarm.cli;

/**
 * @author Bob McWhirter
 */
public class ParseState {

    private final String[] args;
    private int cur;

    public ParseState(String...args) {
        this.args = args;
        this.cur = 0;
    }

    public String la() {
        if ( this.cur >= this.args.length ) {
            return null;
        }
        return this.args[this.cur];
    }

    public String consume() {
        if ( la() == null ) {
            throw new RuntimeException( "parse error" );
        }
        return this.args[ this.cur++ ];
    }
}
