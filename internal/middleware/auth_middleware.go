package middleware

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

type APIKeyValidator interface {
    ValidateAPIKey(key string) (string, error)
}

// Example implementation of validator
type StaticAPIKeyValidator struct {
    Keys map[string]string // apiKey -> userID
}

func (v *StaticAPIKeyValidator) ValidateAPIKey(key string) (string, error) {
		fmt.Println("-------ss------1%w", key)
		fmt.Println("-------ss------1%w", v.Keys)

    userID, ok := v.Keys["my-secret-api-key"]
    if !ok {
        return "", fmt.Errorf("invalid api key")
    }
    return userID, nil
}

func APIKeyAuthMiddleware(validator APIKeyValidator) gin.HandlerFunc {
    return func(c *gin.Context) {
        apiKey := c.GetHeader("x-api-key")
        if apiKey == "" {
            c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "missing api key"})
            return
        }

        userID, err := validator.ValidateAPIKey(apiKey)
        if err != nil {
            c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid api key"})
            return
        }

        // Store actor in context for policy system
        c.Set("actor", "user:"+userID)
        c.Next()
    }
}
