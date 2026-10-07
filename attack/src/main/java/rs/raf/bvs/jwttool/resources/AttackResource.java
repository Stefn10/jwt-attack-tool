package rs.raf.bvs.jwttool.resources;

import rs.raf.bvs.jwttool.attacks.A1_TokenDecoder;
import rs.raf.bvs.jwttool.attacks.A2_TokenForgery;
import rs.raf.bvs.jwttool.attacks.A3_BruteForceSecret;
import rs.raf.bvs.jwttool.attacks.A4_AlgNoneAttack;
import rs.raf.bvs.jwttool.attacks.A5_ExpiredReplay;
import rs.raf.bvs.jwttool.attacks.A6_PrivilegeEscalation;
import rs.raf.bvs.jwttool.attacks.A7_AlgorithmConfusion;
import rs.raf.bvs.jwttool.attacks.A8_KidInjection;
import rs.raf.bvs.jwttool.attacks.A9_NoRevocation;
import rs.raf.bvs.jwttool.attacks.A10_TokenSurfaceAudit;
import rs.raf.bvs.jwttool.attacks.Attack;
import rs.raf.bvs.jwttool.attacks.AttackContext;
import rs.raf.bvs.jwttool.attacks.AttackResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/attack")
public class AttackResource {

    @POST
    @Path("/{n}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runAttack(@PathParam("n") int n, AttackContext ctx) {
        if (ctx == null) ctx = new AttackContext();
        Attack attack = pick(n);
        if (attack == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unknown attack id: " + n);
            return Response.status(404).entity(err).build();
        }
        try {
            AttackResult result = attack.run(ctx);
            return Response.ok(result).build();
        } catch (Exception e) {
            AttackResult err = new AttackResult("Attack " + n + " - error");
            err.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
            err.setSuccess(false);
            return Response.status(500).entity(err).build();
        }
    }

    private Attack pick(int n) {
        switch (n) {
            case 1: return new A1_TokenDecoder();
            case 2: return new A2_TokenForgery();
            case 3: return new A3_BruteForceSecret();
            case 4: return new A4_AlgNoneAttack();
            case 5: return new A5_ExpiredReplay();
            case 6: return new A6_PrivilegeEscalation();
            case 7: return new A7_AlgorithmConfusion();
            case 8: return new A8_KidInjection();
            case 9: return new A9_NoRevocation();
            case 10: return new A10_TokenSurfaceAudit();
            default: return null;
        }
    }
}
