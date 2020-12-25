Service for connect with LINE

Command that supported
    1. release: get current release branch in git
        argument:   
            - service name [inv, po, gr, payment, cn, dn, request, aggregate, common]
        usage: release [service name]
        example: release inv
    2. jira: Track information of target story
        argument: 
            - Target story: BLOC-XXX
            - Tatget tag: 
                - fixversion : release branch that merge
                - status : current status
        usage: jira [story] [tag(optional)]
        example: jira BLOC-13500 status
        show information example: jira BLOC-13500 
