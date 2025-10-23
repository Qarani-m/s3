package application

import (
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"s3/internal/domain"
	"s3/internal/infrastructure/dto"
	"time"

	"github.com/google/uuid"
)

type PresignService struct {
	repo      domain.RepositoryPort
	storage   domain.StoragePort
	secretKey string
}

func NewPresignService(repo domain.RepositoryPort, storage domain.StoragePort, secretKey string) *PresignService {
	return &PresignService{
		repo:      repo,
		storage:   storage,
		secretKey: secretKey,
	}

}

// GenerateUploadURL creates a presigned URL for uploading
func (s *PresignService) GenerateUploadURL(ctx context.Context, input dto.GenerateUploadURLInput) (*dto.GenerateUploadURLOutput, error) {
	if input.Key == "" {
		return nil, fmt.Errorf("key is required")
	}

	// Verify bucket exists
	bucket, err := s.repo.GetBucketByID(ctx, input.BucketID)
	if err != nil {
		return nil, fmt.Errorf("bucket not found: %w", err)
	}

	expiresIn := input.ExpiresIn
	if expiresIn == 0 {
		expiresIn = 3600 // 1 hour default
	}

	urlID := uuid.New().String()
	expiresAt := time.Now().Add(time.Duration(expiresIn) * time.Second)

	// Generate signed URL
	url := s.generateSignedURL(urlID, bucket.Name, input.Key, "PUT", expiresAt)

	// Save presigned URL metadata
	presignedURL := &domain.PresignedURL{
		ID:        urlID,
		BucketID:  input.BucketID,
		Key:       input.Key,
		Type:      "upload",
		ExpiresAt: expiresAt,
		Revoked:   false,
		Metadata:  input.Metadata,
		CreatedAt: time.Now(),
	}

	if err := s.repo.SavePresignedURL(ctx, presignedURL); err != nil {
		return nil, fmt.Errorf("failed to save presigned URL: %w", err)
	}

	return &dto.GenerateUploadURLOutput{
		URL:       url,
		URLID:     urlID,
		ExpiresAt: expiresAt,
		Fields: map[string]string{
			"Content-Type": input.ContentType,
		},
	}, nil
}

func (s *PresignService) generateSignedURL(urlID, bucket, key, method string, expiresAt time.Time) string {
	baseURL := fmt.Sprintf("/api/v1/buckets/%s/objects/%s", bucket, key)
	params := fmt.Sprintf("urlId=%s&expires=%d&method=%s", urlID, expiresAt.Unix(), method)

	signature := s.signString(params)

	return fmt.Sprintf("%s?%s&signature=%s", baseURL, params, signature)
}

func (s *PresignService) signString(data string) string {
	h := hmac.New(sha256.New, []byte(s.secretKey))
	h.Write([]byte(data))
	return hex.EncodeToString(h.Sum(nil))
}

// GenerateDownloadURL creates a presigned URL for downloading
func (s *PresignService) GenerateDownloadURL(ctx context.Context, input dto.GenerateDownloadURLInput) (*dto.GenerateDownloadURLOutput, error) {
	// Verify file exists
	file, err := s.repo.GetFileByID(ctx, input.FileID)
	if err != nil {
		return nil, fmt.Errorf("file not found: %w", err)
	}

	if file.BucketID != input.BucketID {
		return nil, fmt.Errorf("file does not belong to specified bucket")
	}

	bucket, err := s.repo.GetBucketByID(ctx, input.BucketID)
	if err != nil {
		return nil, fmt.Errorf("bucket not found: %w", err)
	}

	expiresIn := input.ExpiresIn
	if expiresIn == 0 {
		expiresIn = 3600
	}

	urlID := uuid.New().String()
	expiresAt := time.Now().Add(time.Duration(expiresIn) * time.Second)

	url := s.generateSignedURL(urlID, bucket.Name, file.Key, "GET", expiresAt)

	presignedURL := &domain.PresignedURL{
		ID:        urlID,
		BucketID:  input.BucketID,
		FileID:    input.FileID,
		Key:       file.Key,
		Type:      "download",
		ExpiresAt: expiresAt,
		Revoked:   false,
		CreatedAt: time.Now(),
	}

	if err := s.repo.SavePresignedURL(ctx, presignedURL); err != nil {
		return nil, fmt.Errorf("failed to save presigned URL: %w", err)
	}

	return &dto.GenerateDownloadURLOutput{
		URL:       url,
		URLID:     urlID,
		ExpiresAt: expiresAt,
	}, nil
}

// RevokePresignedURL revokes a presigned URL
func (s *PresignService) RevokePresignedURL(ctx context.Context, urlID string) error {
	presignedURL, err := s.repo.GetPresignedURLByID(ctx, urlID)
	if err != nil {
		return fmt.Errorf("presigned URL not found: %w", err)
	}

	presignedURL.Revoked = true
	if err := s.repo.UpdatePresignedURL(ctx, presignedURL); err != nil {
		return fmt.Errorf("failed to revoke presigned URL: %w", err)
	}

	return nil
}

// ListPresignedURLs lists active presigned URLs
func (s *PresignService) ListPresignedURLs(ctx context.Context, input dto.ListPresignedURLsInput) (*dto.ListPresignedURLsOutput, error) {
	urls, err := s.repo.ListPresignedURLs(ctx, input.BucketID, input.Limit)
	if err != nil {
		return nil, fmt.Errorf("failed to list presigned URLs: %w", err)
	}

	urlInfos := make([]dto.PresignedURLInfo, len(urls))
	for i, url := range urls {
		urlInfos[i] = dto.PresignedURLInfo{
			ID:        url.ID,
			BucketID:  url.BucketID,
			Key:       url.Key,
			Type:      url.Type,
			ExpiresAt: url.ExpiresAt,
			Revoked:   url.Revoked,
			CreatedAt: url.CreatedAt,
		}
	}

	return &dto.ListPresignedURLsOutput{
		URLs: urlInfos,
	}, nil
} 



// ValidatePresignedURL checks if a presigned URL is valid and usable
func (s *PresignService) ValidatePresignedURL(ctx context.Context, input dto.ValidatePresignedURLInput) (*dto.ValidatePresignedURLOutput, error) {
	// Get presigned URL
	presignedURL, err := s.repo.GetPresignedURLByID(ctx, input.URLID)
	if err != nil {
		return &dto.ValidatePresignedURLOutput{
			Valid:  false,
			Reason: "URL not found",
		}, nil
	}

	// Check if revoked
	if presignedURL.Revoked {
		return &dto.ValidatePresignedURLOutput{
			Valid:  false,
			Reason: "URL has been revoked",
		}, nil
	}

	// Check if expired
	if time.Now().After(presignedURL.ExpiresAt) {
		return &dto.ValidatePresignedURLOutput{
			Valid:  false,
			Reason: "URL has expired",
		}, nil
	}

	// Valid URL
	return &dto.ValidatePresignedURLOutput{
		Valid:     true,
		BucketID:  presignedURL.BucketID,
		Key:       presignedURL.Key,
		Type:      presignedURL.Type,
		ExpiresAt: presignedURL.ExpiresAt,
	}, nil
}




// GenerateMultipartUploadURLs creates presigned URLs for multipart upload
func (s *PresignService) GenerateMultipartUploadURLs(ctx context.Context, input dto.GenerateMultipartUploadURLsInput) (*dto.GenerateMultipartUploadURLsOutput, error) {
	if input.Key == "" {
		return nil, fmt.Errorf("key is required")
	}
	if input.Parts < 1 {
		return nil, fmt.Errorf("parts must be at least 1")
	}

	// Verify bucket exists
	bucket, err := s.repo.GetBucketByID(ctx, input.BucketID)
	if err != nil {
		return nil, fmt.Errorf("bucket not found: %w", err)
	}

	expiresIn := input.ExpiresIn
	if expiresIn == 0 {
		expiresIn = 3600 // 1 hour default
	}

	uploadID := uuid.New().String()
	expiresAt := time.Now().Add(time.Duration(expiresIn) * time.Second)

	// Generate presigned URLs for each part
	parts := make([]dto.MultipartURLPart, input.Parts)
	for i := 0; i < input.Parts; i++ {
		partNumber := i + 1
		urlID := uuid.New().String()
		
		// Generate signed URL with part number
		url := s.generateMultipartSignedURL(urlID, bucket.Name, input.Key, partNumber, "PUT", expiresAt)
		
		parts[i] = dto.MultipartURLPart{
			PartNumber: partNumber,
			URL:        url,
		}

		// Save presigned URL metadata for each part
		presignedURL := &domain.PresignedURL{
			ID:        urlID,
			BucketID:  input.BucketID,
			Key:       input.Key,
			Type:      "multipart",
			ExpiresAt: expiresAt,
			Revoked:   false,
			Metadata: map[string]string{
				"upload_id":   uploadID,
				"part_number": fmt.Sprintf("%d", partNumber),
				"content_type": input.ContentType,
			},
			CreatedAt: time.Now(),
		}

		// Merge user metadata
		for k, v := range input.Metadata {
			presignedURL.Metadata[k] = v
		}

		if err := s.repo.SavePresignedURL(ctx, presignedURL); err != nil {
			return nil, fmt.Errorf("failed to save presigned URL for part %d: %w", partNumber, err)
		}
	}

	return &dto.GenerateMultipartUploadURLsOutput{
		UploadID:  uploadID,
		Parts:     parts,
		ExpiresAt: expiresAt,
	}, nil
}

// generateMultipartSignedURL generates a signed URL for multipart upload part
func (s *PresignService) generateMultipartSignedURL(urlID, bucketName, key string, partNumber int, method string, expiresAt time.Time) string {
	expires := expiresAt.Unix()
	data := fmt.Sprintf("%s:%s:%s:%d:%d:%s", urlID, bucketName, key, partNumber, expires, method)
	
	hash := hmac.New(sha256.New, []byte(s.secretKey))
	hash.Write([]byte(data))
	signature := hex.EncodeToString(hash.Sum(nil))

	return fmt.Sprintf("/api/v1/buckets/%s/objects/%s?urlId=%s&part=%d&expires=%d&method=%s&signature=%s",
		bucketName, key, urlID, partNumber, expires, method, signature)
}