package bgu.bioinf.rnaSequenceSniffer.webInterface;

import bgu.bioinf.rnaSequenceSniffer.db.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matan on 27/11/14.
 */
public class JobRetriever {
    private JobEntity jobEntity;

    public JobBpMatrixEntity getJobBpMatrixEntity() {
        return jobBpMatrixEntity;
    }

    private JobBpMatrixEntity jobBpMatrixEntity;
    private boolean accessError;
    private long noOfReults;

    public JobRetriever() {
        jobEntity = null;
    }

    /**
     * initiates the job by the given job Id
     *
     * @param jobId The jobId of the job we are looking for
     * @return True if the job jobId exists
     */
    public boolean initJob(String jobId) {
        this.accessError = false;
        EntityManager em = null;
        boolean result = false;
        try {
            em = DBConnector.getEntityManager();
            jobEntity = em.find(JobEntity.class, jobId);
            if (jobEntity != null) {
                jobBpMatrixEntity = em.find(JobBpMatrixEntity.class, jobId);
                result = true;
            }
        } catch (Exception e) {
            accessError = true;
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }
        return result;
    }

    public boolean isAccessError() {
        return accessError;
    }

    public long getNoOfResults(List<Float> filters) {
        long results;
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<JobResultEntity> root = criteriaQuery.from(JobResultEntity.class);
            ParameterExpression<String> jobIdParam = criteriaBuilder.parameter(String.class);
            criteriaQuery.select(criteriaBuilder.count(root));
            Predicate where = criteriaBuilder.equal(root.get("jobId"), jobEntity.getJobId());
            if (filters != null) {
                Map<Expression<Boolean>, ParameterExpression<Integer>> addedWheres = new HashMap<Expression<Boolean>, ParameterExpression<Integer>>();

                if (filters.get(0) != null) {
                    ParameterExpression<Float> matrixParam = criteriaBuilder.parameter(Float.class);
                    if (where != null) {
                        where = criteriaBuilder.and(where, criteriaBuilder.le(root.<Float>get("matrixScore"), filters.get(0)));
                    }
                }
                if (filters.get(1) != null) {
                    ParameterExpression<Float> energyParam = criteriaBuilder.parameter(Float.class);
                    if (where != null) {
                        where = criteriaBuilder.and(where, criteriaBuilder.le(root.<Float>get("energyScore"), filters.get(1)));
                    }
                }
            }
            criteriaQuery.where(where);
            TypedQuery<Long> query = em.createQuery(criteriaQuery);
            results = query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            results = 0;
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }
        return results;
    }

    /**
     * If the job was initiated, this function will return all relevant results for this job
     *
     * @param maxResults Max amount of results to return
     * @param page       We bring results [page * maxResults, page * maxResults + maxResults)
     * @param sortBy     String that specify which column to sort by (after target)
     * @param filters    list of Floats with filter number (currently 0 - max martrix score, 1 - max energy score)
     * @return all the relevant results for this job
     */
    public Map<Integer, List<JobResultEntity>> getAllResults(long maxResults, int page, String sortBy, List<Float> filters) {
        Map<Integer, List<JobResultEntity>> resultMap;
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<JobResultEntity> criteriaQuery = criteriaBuilder.createQuery(JobResultEntity.class);
            Root<JobResultEntity> root = criteriaQuery.from(JobResultEntity.class);
            ParameterExpression<String> jobIdParam = criteriaBuilder.parameter(String.class);
            criteriaQuery.select(root);
            Predicate where = criteriaBuilder.equal(root.get("jobId"), jobEntity.getJobId());
            if (filters != null) {
                Map<Expression<Boolean>, ParameterExpression<Integer>> addedWheres = new HashMap<Expression<Boolean>, ParameterExpression<Integer>>();

                if (filters.get(0) != null) {
                    ParameterExpression<Float> matrixParam = criteriaBuilder.parameter(Float.class);
                    if (where != null) {
                        where = criteriaBuilder.and(where, criteriaBuilder.le(root.<Float>get("matrixScore"), filters.get(0)));
                    }
                }
                if (filters.get(1) != null) {
                    ParameterExpression<Float> energyParam = criteriaBuilder.parameter(Float.class);
                    if (where != null) {
                        where = criteriaBuilder.and(where, criteriaBuilder.le(root.<Float>get("energyScore"), filters.get(1)));
                    }
                }
            }
            criteriaQuery.where(where);
            List<Order> orderBy = analyzeSortBy(criteriaBuilder, root, sortBy);
            criteriaQuery.orderBy(orderBy);
            TypedQuery<JobResultEntity> query = em.createQuery(criteriaQuery);
            //em.createNamedQuery("JobResult.GetAllByJobID", JobResultEntity.class);
            query.setFirstResult((int) (page * maxResults));
            query.setMaxResults((int) maxResults);
            //query.setParameter("jobId", jobEntity.getJobId());

            resultMap = new HashMap<Integer, List<JobResultEntity>>();
            for (JobResultEntity jobResultsEntity : query.getResultList()) {
                jobResultsEntity.setAlignedStructure(jobEntity.getQueryStructure());
                List<JobResultEntity> resultForTarget = resultMap.get(jobResultsEntity.getTargetNo());
                if (resultForTarget == null) {
                    resultForTarget = new ArrayList<JobResultEntity>();
                }
                resultForTarget.add(jobResultsEntity);
                resultMap.put(jobResultsEntity.getTargetNo(), resultForTarget);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap = null;
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }
        return resultMap;
    }

    private List<Order> analyzeSortBy(CriteriaBuilder cb, Root<JobResultEntity> root, String sortBy) {
        List<Order> orderBy = new ArrayList<Order>();

        orderBy.add(cb.asc(root.get("targetNo")));
        if (sortBy.startsWith("startIndex")) {
            if (!sortBy.endsWith("DESC")) {
                orderBy.add(cb.asc(root.get("startIndex")));
            } else {
                orderBy.add(cb.desc(root.get("startIndex")));
            }
            orderBy.add(cb.asc(root.get("gapStr")));
        } else if (sortBy.startsWith("gapStr")) {
            orderBy.add(cb.asc(root.get("startIndex")));
            if (!sortBy.endsWith("DESC")) {
                orderBy.add(cb.asc(root.get("gapStr")));
            } else {
                orderBy.add(cb.desc(root.get("gapStr")));
            }
        } else {
            try {
                String[] sort = sortBy.split("_");
                if (!"DESC".equals(sort[1])) {
                    orderBy.add(cb.asc(root.get(sort[0])));
                } else {
                    orderBy.add(cb.desc(root.get(sort[0])));
                }
            } catch (Exception ignore) {
                if (!"".equals(sortBy))
                    System.err.println("unknown sort type: \"" + sortBy + "\"");
            }
            orderBy.add(cb.asc(root.get("startIndex")));
            orderBy.add(cb.asc(root.get("gapStr")));
        }

        return orderBy;
    }

    public JobEntity getJobEntity() {
        return jobEntity;
    }

    public boolean isReady() {
        return jobEntity.getEndTime() != null;
    }

    public String getError() {
        String error = "";
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            JobErrorEntity errorsEntity = em.find(JobErrorEntity.class, jobEntity.getJobId());
            error = errorsEntity.getErrorStr();
        } catch (Exception ignore) {
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }
        return error;
    }

    public List<JobEntity> getAllJobsWithName(String queryName) {
        List<JobEntity> jobList = null;
        EntityManager em = null;

        try {
            em = DBConnector.getEntityManager();
            TypedQuery<JobEntity> jobIdQuery = em.createNamedQuery("Job.GetAllByQname", JobEntity.class);
            jobIdQuery.setParameter("queryNamePatt", "%" + queryName + "%");
            jobList = jobIdQuery.getResultList();
        } catch (Exception ignore) {
            jobList = null;
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }

        return jobList;
    }
}
