package application

import (
	"context"
	"fmt"
	"s3/internal/domain"
)

type DeleteService struct {
	storage    domain.StoragePort
	repository domain.RepositoryPort
}

func NewDeleteService(storage domain.StoragePort, repository domain.RepositoryPort) *DeleteService {
	return &DeleteService{
		storage:    storage,
		repository: repository,
	}
}

type DeleteFileInput struct {
	FileID   string
	BucketID string
	Key      string
}

func (s *DeleteService) DeleteFile(ctx context.Context, input DeleteFileInput) error {
	// 1. Delete from storage (MinIO)
	err := s.storage.DeleteObject(ctx, input.BucketID, input.Key)
	if err != nil {
		return fmt.Errorf("failed to delete object from storage: %w", err)
	}

	// 2. Delete metadata from database
	err = s.repository.DeleteFile(ctx, input.FileID)
	if err != nil {
		return fmt.Errorf("failed to delete file metadata: %w", err)
	}

	return nil
}