package http

import (
	"net/http"

	"s3/internal/application"
	"s3/internal/infrastructure/dto"

	"github.com/gin-gonic/gin"
)

type AnalyticsHandler struct {
	analyticsService *application.AnalyticsService
}

func NewAnalyticsHandler(analyticsService *application.AnalyticsService) *AnalyticsHandler {
	return &AnalyticsHandler{analyticsService: analyticsService}
}

func (h *AnalyticsHandler) GetStorageUsage(c *gin.Context) {
	output, err := h.analyticsService.GetStorageUsage(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, output)
}

func (h *AnalyticsHandler) GetTrafficStats(c *gin.Context) {
	var input dto.GetTrafficStatsInput
	if err := c.ShouldBindQuery(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid query parameters"})
		return
	}

	output, err := h.analyticsService.GetTrafficStats(c.Request.Context(), input)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, output)
}

func (h *AnalyticsHandler) GetFileTypeDistribution(c *gin.Context) {
	output, err := h.analyticsService.GetFileTypeDistribution(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, output)
}

func (h *AnalyticsHandler) GetBucketUsageOverTime(c *gin.Context) {
	bucketId := c.Param("bucketId")

	var input dto.GetBucketUsageOverTimeInput
	if err := c.ShouldBindQuery(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid query parameters"})
		return
	}

	output, err := h.analyticsService.GetBucketUsageOverTime(c.Request.Context(), bucketId, input)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, output)
}

func (h *AnalyticsHandler) GetPopularFiles(c *gin.Context) {
	var input dto.GetPopularFilesInput
	if err := c.ShouldBindQuery(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid query parameters"})
		return
	}

	output, err := h.analyticsService.GetPopularFiles(c.Request.Context(), input)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, output)
}

func (h *AnalyticsHandler) GetUserActivity(c *gin.Context) {
	userId := c.Param("userId")

	output, err := h.analyticsService.GetUserActivity(c.Request.Context(), userId)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, output)
}

func (h *AnalyticsHandler) ExportAnalytics(c *gin.Context) {
	var input dto.ExportAnalyticsInput
	if err := c.ShouldBindQuery(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid query parameters"})
		return
	}

	data, contentType, err := h.analyticsService.ExportAnalytics(c.Request.Context(), input)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.Header("Content-Type", contentType)
	c.Header("Content-Disposition", "attachment; filename=analytics."+input.Format)
	c.Data(http.StatusOK, contentType, data)
}

func (h *AnalyticsHandler) GetAPIUsage(c *gin.Context) {
	output, err := h.analyticsService.GetAPIUsage(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, output)
}