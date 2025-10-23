package dto

import "time"

// CreateBucketOutput defines data returned after creating a bucket.
type CreateBucketOutput struct {
    BucketID  string    `json:"bucket_id"`
    Name      string    `json:"name"`
    CreatedAt time.Time `json:"created_at"`
    OwnerID   string    `json:"owner_id"`
}


type GetBucketOutput struct {
	BucketID  string    `json:"bucket_id"`
	Name      string    `json:"name"`
	CreatedAt time.Time `json:"created_at"`
}

type UpdateBucketInput struct {
	Name string `json:"name,omitempty"`
}

type UpdatePolicyInput struct {
	Policy string `json:"policy"`
}

type VersioningInput struct {
	Enabled bool `json:"enabled"`
}
type VersioningOutput struct {
	Enabled bool   `json:"enabled"`
	Status  string `json:"status"` // "Enabled", "Suspended", or ""
}
type LifecycleInput struct {
	Rules []LifecycleRule `json:"rules"`
}

type LifecycleRule struct {
	ID         string `json:"id"`
	Prefix     string `json:"prefix"`
	Expiration int    `json:"expiration_days"`
}

type BucketStatsOutput struct {
	BucketID   string `json:"bucket_id"`
	TotalFiles int64  `json:"total_files"`
	TotalSize  int64  `json:"total_size_bytes"`
}

type BucketPolicyOutput struct {
	BucketID string `json:"bucket_id"`
	Policy   string `json:"policy"`
}



type CreateBucketInput struct {
	Name string `json:"name"`
	OwnerId string 	`json:"owner_id"`
}



