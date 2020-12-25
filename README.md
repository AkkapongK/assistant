**Service for connect with LINE**

Command that supported
    
    
   1. **release**: get current release branch in git
        
        argument:    
        - service name [inv, po, gr, payment, cn, dn, request, aggregate, common]
        
        usage: release [service name]
        
        _example_: release inv
        
   2. **jira**: Track information of target story
        
        argument: 
        - Target story: BLOC-XXX
        - Tatget tag: 
            - fixversion : release branch that merge
            - status : current status
        
        usage: jira [story] [tag(optional)]
        
        _example_: jira BLOC-13500 status
        
        _show information example_: jira BLOC-13500 