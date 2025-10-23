package domain

import (
	"context"
	"s3/internal/infrastructure/dto"
	"time"
)

type StoragePort interface {
	SaveObject(ctx context.Context, bucket, key string, data []byte, metadata map[string]string) error
	GetObject(ctx context.Context, bucket, key string) ([]byte, error)
	DeleteObject(ctx context.Context, bucket, key string) error
	CreateBucket(ctx context.Context, name string) (string,error)
	DeleteBucket(ctx context.Context, bucketId string) error

	SetBucketVersioning(ctx context.Context, name string, enabled bool) error
	SetBucketLifecycle(ctx context.Context, name string, input dto.LifecycleInput) error

	CopyObject(
		ctx context.Context,
		srcBucket string,
		srcKey string,
		dstBucket string,
		dstKey string,
	) error
	GetBucketVersioning(ctx context.Context, bucketId string) (*dto.VersioningOutput, error)
}

type RepositoryPort interface {
	// Files
	SaveFile(ctx context.Context, file File) error
	GetFileByID(ctx context.Context, id string) (*File, error)
	GetFileByKey(ctx context.Context, bucketID, key string) (*File, error)
	ListFiles(ctx context.Context, bucketID string) ([]File, error)
	UpdateFile(ctx context.Context, file *File) error
	DeleteFile(ctx context.Context, id string) error

	// Buckets
	SaveBucket(ctx context.Context, bucket *Bucket) (Bucket, error)
	GetBucketByID(ctx context.Context, bucketId string) (Bucket, error)
	GetBucketByName(ctx context.Context, name string) (Bucket, error)
	ListBuckets(ctx context.Context) ([]Bucket, error)
	UpdateBucket(ctx context.Context, bucket *Bucket) (*Bucket, error)
	DeleteBucket(ctx context.Context, bucketId string) error

	// Presigned URLs
	SavePresignedURL(ctx context.Context, presignedUrl *PresignedURL) error
	ListPresignedURLs(ctx context.Context, bucketID string, limit int) ([]PresignedURL, error)
	GetPresignedURLByID(ctx context.Context, id string) (*PresignedURL, error)
	UpdatePresignedURL(ctx context.Context, presignedUrl *PresignedURL) error

	// Batch Operations
	SaveBatchOperation(ctx context.Context, operation *BatchOperation) error
	GetBatchOperationByID(ctx context.Context, id string) (*BatchOperation, error)
	ListBatchOperations(ctx context.Context, status, opType string, limit int) ([]BatchOperation, error)
	UpdateBatchOperation(ctx context.Context, operation *BatchOperation) error

	// Files by prefix
	ListFilesByPrefix(ctx context.Context, bucketID, prefix string, limit int) ([]File, error)
	CountFilesByPrefix(ctx context.Context, bucketID, prefix string) (int, error)

	// Search
	SearchFilesByName(ctx context.Context, bucketID, query string, limit int) ([]File, error)
	SearchFilesByMetadata(ctx context.Context, bucketID string, metadata map[string]string, limit int) ([]File, error)
	SearchFilesByTags(ctx context.Context, bucketID string, tags []string, limit int) ([]File, error)
	AdvancedSearchFiles(ctx context.Context, input dto.AdvancedSearchInput) ([]File, error)
	GetSearchSuggestions(ctx context.Context, bucketID, query string, limit int) ([]string, error)

	// Search History
	SaveSearchHistory(ctx context.Context, history *SearchHistory) error
	GetSearchHistory(ctx context.Context, limit int) ([]SearchHistory, error)
	SaveSearchQuery(ctx context.Context, search *SavedSearch) error

	// Webhooks
	SaveWebhook(ctx context.Context, webhook *Webhook) error
	GetWebhookByID(ctx context.Context, id string) (*Webhook, error)
	ListWebhooksByBucket(ctx context.Context, bucketID string) ([]Webhook, error)
	UpdateWebhook(ctx context.Context, webhook *Webhook) error
	DeleteWebhook(ctx context.Context, id string) error
	SaveWebhookDelivery(ctx context.Context, delivery *WebhookDelivery) error
	ListWebhookDeliveries(ctx context.Context, webhookID string, limit int) ([]WebhookDelivery, error)

	// Analytics
	GetAccessLogsByDateRange(ctx context.Context, start, end time.Time) ([]AccessLog, error)
	GetAccessLogsByUser(ctx context.Context, userID string, limit int) ([]AccessLog, error)
	GetPopularFiles(ctx context.Context, limit int) ([]struct {
		FileID, Key string
		AccessCount int
		TotalSize   int64
	}, error)
	SaveAccessLog(ctx context.Context, log *AccessLog) error

	// Multipart Uploads
	SaveMultipartUpload(ctx context.Context, upload *MultipartUpload) error
	GetMultipartUploadByUploadID(ctx context.Context, uploadID string) (*MultipartUpload, error)
	UpdateMultipartUpload(ctx context.Context, upload *MultipartUpload) error
	ListMultipartUploadsByBucket(ctx context.Context, bucketID string) ([]MultipartUpload, error)


	// 🔐 Policy-related operations
	IncrementPolicyVersionAndUpdateBucket(ctx context.Context, bucket *Bucket) error
	AppendPolicyHistory(ctx context.Context, bucketID string, policy *Policy, actor string) error

}

type Logger interface {
	Info(ctx context.Context, msg string, fields map[string]interface{})
	Error(ctx context.Context, msg string, fields map[string]interface{})
	Debug(ctx context.Context, msg string, fields map[string]interface{})
}

type SystemPort interface {
	HealthCheck() error
}

type EventPublisher interface {
	Publish(ctx context.Context, topic string, payload interface{}) error
	Consume(ctx context.Context, topic string) error
}
