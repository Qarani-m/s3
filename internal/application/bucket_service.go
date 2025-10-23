package application

import (
	"context"
 
	"fmt"
	"s3/internal/domain"
	"s3/internal/infrastructure/dto"
	"time" 
)

// BucketService provides business logic for managing buckets.
type BucketService struct {
	repo    domain.RepositoryPort
	storage domain.StoragePort
}

type BucketAlreadyExists struct {
	Name string
}

// NewBucketService creates a new instance of BucketService.
func NewBucketService(repo domain.RepositoryPort, storage domain.StoragePort) *BucketService {
	return &BucketService{
		repo:    repo,
		storage: storage,
	}
}
func (e *BucketAlreadyExists) Error() string {
    return fmt.Sprintf("bucket %s already exists", e.Name)
}

func (s *BucketService) CreateBucket(ctx context.Context, input dto.CreateBucketInput) (*dto.CreateBucketOutput, error) {
	if input.Name == "" {
		return nil, fmt.Errorf("bucket name is required")
	}

	// Try to create bucket in storage
	bucketId, err := s.storage.CreateBucket(ctx, input.Name)
	if err != nil {
		// Handle "bucket already exists" gracefully using type assertion
		if _, ok := err.(*BucketAlreadyExists); ok {
			return nil, fmt.Errorf("bucket already exists: %s", input.Name)
		}
		return nil, fmt.Errorf("%w", err)
	}

	// Create bucket metadata
	bucket := &domain.Bucket{
		ID:        bucketId,
		Name:      input.Name,
		OwnerID:   input.OwnerId,
		CreatedAt: time.Now(),
	}

	// Save metadata in repository
	bucketObject, err := s.repo.SaveBucket(ctx, bucket)
	if err != nil {
		return nil, fmt.Errorf("failed to save bucket metadata: %w", err)
	}

	// Return success
	return &dto.CreateBucketOutput{
		BucketID:  bucketObject.ID,
		Name:      input.Name,
		CreatedAt: bucket.CreatedAt,
	}, nil
}


func (s *BucketService) GetBucket(ctx context.Context, bucketID string) (*dto.GetBucketOutput, error) {
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)
	if err != nil {
		return nil, fmt.Errorf("bucket not found: %w", err)
	}

	return &dto.GetBucketOutput{
		BucketID:  bucket.ID,
		Name:      bucket.Name,
		CreatedAt: bucket.CreatedAt,
	}, nil
}

func (s *BucketService) ListBuckets(ctx context.Context) ([]domain.Bucket, error) {
	return s.repo.ListBuckets(ctx)
}

func (s *BucketService) UpdateBucket(ctx context.Context, bucketID string, input dto.UpdateBucketInput) (*dto.GetBucketOutput, error) {
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)
	if err != nil {
		return nil, fmt.Errorf("bucket not found: %w", err)
	}

	if input.Name != "" {
		bucket.Name = input.Name
		bucket.UpdatedAt = time.Now()
	}

	updated, err := s.repo.UpdateBucket(ctx, &bucket)
	if err != nil {
		return nil, fmt.Errorf("failed to update bucket: %w", err)
	}

	return &dto.GetBucketOutput{
		BucketID:  updated.ID,
		Name:      updated.Name,
		CreatedAt: updated.CreatedAt,
	}, nil
}

func (s *BucketService) DeleteBucket(ctx context.Context, bucketID string) error {
	files, err := s.repo.ListFiles(ctx, bucketID)
	if err != nil {
		return fmt.Errorf("failed to check files: %w", err)
	}



	if len(files) > 0 {
		return fmt.Errorf("cannot delete bucket with files")
	}
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)
 na:= fmt.Sprintf("bt-%s", bucket.Name)
	if err != nil {
		return fmt.Errorf("bucket not found: %w", err)
	}
	if err := s.storage.DeleteBucket(ctx,na); err != nil {
		return fmt.Errorf("failed to delete from storage: %w", err)
	}
	if err := s.repo.DeleteBucket(ctx, bucketID); err != nil {
		return fmt.Errorf("failed to delete bucket: %w", err)
	}
	return nil
}

func (s *BucketService) GetBucketStats(ctx context.Context, bucketID string) (*dto.BucketStatsOutput, error) {
	files, err := s.repo.ListFiles(ctx, bucketID)
	if err != nil {
		return nil, fmt.Errorf("failed to get files: %w", err)
	}
	
	var totalSize int64
	for _, f := range files {
		totalSize += f.Size
	}
	
	return &dto.BucketStatsOutput{
		BucketID:   bucketID,
		TotalFiles: int64(len(files)),
		TotalSize:  totalSize,
	}, nil
}

func (s *BucketService) GetBucketPolicy(ctx context.Context, bucketID string) (*dto.BucketPolicyOutput, error) {
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)

	// a0e8a740-7989-4302-9fb3-f25aec4ae91b

		fmt.Println("----")

	fmt.Println(bucket.ID)
	fmt.Println("----")
	
	if err != nil {
		return nil, fmt.Errorf("bucket not found: %w", err)
	}
	
	policyStr := ""
	if bucket.Policy != nil {
		policyStr = bucket.Policy.String()
	}
	
	return &dto.BucketPolicyOutput{
		BucketID: bucketID,
		Policy:   policyStr,
	}, nil
}

func (s *BucketService) UpdateBucketPolicy(ctx context.Context, bucketID string, input dto.UpdatePolicyInput) error {
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)
	if err != nil {
		return fmt.Errorf("bucket not found: %w", err)
	}
	bucket.Policy = &domain.Policy{}
	bucket.UpdatedAt = time.Now()
	
	if _, err := s.repo.UpdateBucket(ctx, &bucket); err != nil {
		return fmt.Errorf("failed to update policy: %w", err)
	}

		fmt.Println(input)
	
	return nil
}

func (s *BucketService) SetBucketVersioning(ctx context.Context, bucketID string, enabled bool) error {
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)
	if err != nil {
		return fmt.Errorf("bucket not found: %w", err)
	}
	fmt.Println("---*--")
	if err := s.storage.SetBucketVersioning(ctx, bucket.Name, enabled); err != nil {
		return fmt.Errorf("failed to set versioning: %w", err)
	}
	
	return nil
}



func (s *BucketService) GetBucketVersioning(ctx context.Context, bucketID string) (*dto.VersioningOutput, error) {
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)
	if err != nil {
		return nil, fmt.Errorf("bucket not found: %w", err)
	}
	
	versioningOutput, err := s.storage.GetBucketVersioning(ctx, bucket.Name)
	if err != nil {
		return nil, fmt.Errorf("failed to get versioning: %w", err)
	}
	
	return versioningOutput, nil
}








func (s *BucketService) SetBucketLifecycle(ctx context.Context, bucketID string, input dto.LifecycleInput) error {
	bucket, err := s.repo.GetBucketByID(ctx, bucketID)
	if err != nil {
		return fmt.Errorf("bucket not found: %w", err)
	}
	if err := s.storage.SetBucketLifecycle(ctx, bucket.Name, input); err != nil {
		return fmt.Errorf("failed to set lifecycle: %w", err)
	}
	
	return nil
}
