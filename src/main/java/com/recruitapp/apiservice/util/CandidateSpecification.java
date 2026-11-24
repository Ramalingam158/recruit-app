package com.recruitapp.apiservice.util;

import com.recruitapp.apiservice.domain.Candidate;
import com.recruitapp.apiservice.domain.CandidateSkill;
import com.recruitapp.apiservice.dto.CandidateFilter;
import com.recruitapp.apiservice.dto.CandidateSearch;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;

@UtilityClass
public class CandidateSpecification {

    public static Specification<Candidate> search(CandidateSearch s) {
        if (s == null) {
            return Specification.unrestricted();
        }

        return Specification.allOf(
                containsCity(s.city(), false),
                containsName(s.name()),
                containsEmail(s.mail())
        );
    }

    public static Specification<Candidate> withFilter(CandidateFilter f) {
        if (f == null) {
            return Specification.unrestricted();
        }

        return Specification.allOf(
                containsCity(f.city(), true),
                minExperience(f.minExp()),
                maxExperience(f.maxExp()),
                maxNoticePeriod(f.maxNoticeDays()),
                hasAnySkill(f.skills())
        );
    }

    private static Specification<Candidate> containsName(String name) {
        return ((root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }

            return cb.like(root.get("name"), like(name));
        });
    }

    private static Specification<Candidate> containsEmail(String mail) {
        return ((root, query, cb) -> {
            if (mail == null || mail.isBlank()) {
                return null;
            }

            return cb.like(root.get("email"), like(mail));
        });
    }

    private static Specification<Candidate> containsCity(String city, boolean exactMatch) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) {
                return null;
            }

            if (exactMatch) {
                return cb.equal(root.get("city"), city.trim().toLowerCase());
            } else {
                return cb.like(root.get("city"), like(city));
            }
        };
    }

    private static Specification<Candidate> minExperience(Integer minExp) {
        return ((root, query, cb) -> {
            if (minExp == null || minExp == 0) {
                return null;
            }

            return cb.greaterThanOrEqualTo(root.get("experienceYears"), minExp);
        });
    }

    private static Specification<Candidate> maxExperience(Integer maxExp) {
        return ((root, query, cb) -> {
            if (maxExp == null) {
                return null;
            }

            return cb.lessThanOrEqualTo(root.get("experienceYears"), maxExp);
        });
    }

    private static Specification<Candidate> maxNoticePeriod(Integer maxNoticePeriod) {
        return ((root, query, cb) -> {
            if (maxNoticePeriod == null) {
                return null;
            }

            return cb.lessThanOrEqualTo(root.get("noticePeriodDays"), maxNoticePeriod);
        });
    }

    private static Specification<Candidate> hasAnySkill(List<String> skills) {
        return (root, query, cb) -> {
            if (skills == null || skills.isEmpty()) {
                return null;
            }

            Set<String> normalized = SkillUtil.normalizeSkills(skills);
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<CandidateSkill> skillRoot = subquery.from(CandidateSkill.class);

            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(skillRoot.get("candidate").get("id"), root.get("id")),
                            skillRoot.get("skill").in(normalized)

                    );

            return cb.exists(subquery);
        };
    }

    private static String like(String str) {
        String escaped = str.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
