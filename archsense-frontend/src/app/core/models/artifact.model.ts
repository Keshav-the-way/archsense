export interface Artifact {
  id: string;
  projectId: string;
  userId: string;
  name: string;
  type: ArtifactType;
  size: number;
  uploadedAt: string;
}

export enum ArtifactType {
  IMAGE = 'IMAGE',
  PDF = 'PDF',
  MARKDOWN = 'MARKDOWN',
  TEXT = 'TEXT',
  OTHER = 'OTHER'
}