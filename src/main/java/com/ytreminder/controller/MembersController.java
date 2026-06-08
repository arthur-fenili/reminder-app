package com.ytreminder.controller;

import com.ytreminder.model.Member;
import com.ytreminder.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MembersController {

    private static final Logger log = LoggerFactory.getLogger(MembersController.class);

    private final MemberRepository memberRepository;

    public MembersController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public List<Member> getAll(@RequestParam(required = false) boolean onlyActive) {
        log.info("GET /api/members — onlyActive={}", onlyActive);
        if (onlyActive) {
            return memberRepository.findByActiveTrue();
        }
        return memberRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getById(@PathVariable Long id) {
        log.info("GET /api/members/{}", id);
        return memberRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Member> create(@RequestBody MemberRequest req) {
        log.info("POST /api/members — criando membro: name={}, email={}", req.name(), req.email());
        Member member = new Member();
        member.setName(req.name());
        member.setEmail(req.email());
        member.setAmount(req.amount());
        memberRepository.save(member);
        log.info("POST /api/members — membro criado com id={}", member.getId());
        return ResponseEntity.created(URI.create("/api/members/" + member.getId())).body(member);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> update(@PathVariable Long id, @RequestBody MemberRequest req) {
        log.info("PUT /api/members/{} — atualizando membro", id);
        return memberRepository.findById(id).map(member -> {
            member.setName(req.name());
            member.setEmail(req.email());
            member.setAmount(req.amount());
            return ResponseEntity.ok(memberRepository.save(member));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long id) {
        log.info("PATCH /api/members/{}/toggle — alternando status", id);
        return memberRepository.findById(id).map(member -> {
            member.setActive(!member.isActive());
            memberRepository.save(member);
            log.info("PATCH /api/members/{}/toggle — novo status: active={}", id, member.isActive());
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("id", member.getId());
            body.put("name", member.getName());
            body.put("active", member.isActive());
            return ResponseEntity.ok(body);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/members/{}", id);
        if (!memberRepository.existsById(id)) return ResponseEntity.notFound().build();
        memberRepository.deleteById(id);
        log.info("DELETE /api/members/{} — membro removido", id);
        return ResponseEntity.noContent().build();
    }

    public record MemberRequest(String name, String email, BigDecimal amount) {}
}
