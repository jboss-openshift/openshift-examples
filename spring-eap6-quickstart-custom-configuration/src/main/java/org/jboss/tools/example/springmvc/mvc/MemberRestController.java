package org.jboss.tools.example.springmvc.mvc;

import java.util.List;

import org.jboss.tools.example.springmvc.domain.Member;
import org.jboss.tools.example.springmvc.repo.MemberDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/members")
public class MemberRestController
{
    @Autowired
    private MemberDao memberDao;

    @RequestMapping(method=RequestMethod.GET, produces="application/json")
    public @ResponseBody List<Member> listAllMembers()
    {
        return memberDao.findAllOrderedByName();
    }

    @RequestMapping(value="/{id}", method=RequestMethod.GET, produces="application/json")
    public @ResponseBody Member lookupMemberById(@PathVariable("id") Long id)
    {
        return memberDao.findById(id);
    }
}
