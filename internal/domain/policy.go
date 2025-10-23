package domain

import (
	"encoding/json"
	"fmt"
)

type Action string

const (
	ActionUpload Action = "upload"
	ActionDelete Action = "delete"
	ActionList   Action = "list"
)

type Policy struct {
	AllowedUsers   []string
	AllowedActions []Action
}

func (p *Policy) Can(userID string, action Action) bool {
	for _, u := range p.AllowedUsers {
		if u == userID {
			for _, a := range p.AllowedActions {
				if a == action {
					return true
				}
			}
		}
	}
	return false
}
func (p *Policy) String() string {
	if p == nil {
		return "<nil>"
	}

	// Convert to JSON for readable structured output
	data, err := json.MarshalIndent(p, "", "  ")
	if err != nil {
		return fmt.Sprintf("Policy{error: %v}", err)
	}

	return string(data)
}