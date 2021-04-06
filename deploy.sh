gcloud functions deploy githubtree \
    --security-level=secure-always \
    --entry-point=githubtree.App \
    --runtime=java11 \
    --trigger-http \
    --region asia-northeast1 \
    --source=output/
