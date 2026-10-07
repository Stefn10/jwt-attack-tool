package rs.raf.bvs.jwttool.attacks;

public interface Attack {
    AttackResult run(AttackContext ctx) throws Exception;
}
